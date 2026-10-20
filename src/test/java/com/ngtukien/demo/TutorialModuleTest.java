package com.ngtukien.demo;

import com.ngtukien.demo.dao.ContractDAO;
import com.ngtukien.demo.dao.MemberDAO;
import com.ngtukien.demo.dao.SponsorDAO;
import com.ngtukien.demo.model.Organizer;
import com.ngtukien.demo.model.Sponsor;
import com.ngtukien.demo.model.enums.CooperationStatus;
import org.apache.catalina.startup.Tomcat;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class TutorialModuleTest {
    private static final String URL = "jdbc:h2:mem:tutorial;DB_CLOSE_DELAY=-1";
    private static Tomcat tomcat;
    private static String base;
    private static int sponsorId;

    @BeforeAll
    static void start() throws Exception {
        System.setProperty("db.url", URL);
        System.setProperty("db.username", "sa");
        System.setProperty("db.password", "");
        try (var connection = DriverManager.getConnection(URL, "sa", "")) {
            RunScript.execute(connection, new InputStreamReader(
                    TutorialModuleTest.class.getResourceAsStream("/schema.sql"), StandardCharsets.UTF_8));
            int adminId = account("admin", "Admin@123");
            try (var statement = connection.prepareStatement("insert into organizer(id,full_name,email) values (?, ?, ?)")) {
                statement.setInt(1, adminId); statement.setString(2, "Organizer");
                statement.setString(3, "admin@example.com"); statement.executeUpdate();
            }
            sponsorId = account("sponsor", "Sponsor@123");
            try (var statement = connection.prepareStatement("""
                    insert into sponsor(id,name,tax_code,field,address,email,tel,status) values (?,?,?,?,?,?,?,?)
                    """)) {
                statement.setInt(1, sponsorId); statement.setString(2, "Sample Sponsor");
                statement.setString(3, "0101234567"); statement.setString(4, "Sports");
                statement.setString(5, "Hanoi"); statement.setString(6, "sponsor@example.com");
                statement.setString(7, "0901234567"); statement.setString(8, "ACTIVE"); statement.executeUpdate();
            }
        }
        tomcat = Main.start(0, Path.of("src/main/webapp"));
        base = "http://localhost:" + tomcat.getConnector().getLocalPort();
    }

    @AfterAll
    static void stop() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
        System.clearProperty("db.url"); System.clearProperty("db.username"); System.clearProperty("db.password");
    }

    @Test
    void loginLoadsTypedProfileAndDoesNotReturnPassword() throws Exception {
        try (MemberDAO dao = new MemberDAO()) {
            var organizer = assertInstanceOf(Organizer.class, dao.checkLogin("admin", "Admin@123"));
            assertEquals("admin@example.com", organizer.getEmail());
            assertNull(organizer.getPassword());
            var sponsor = assertInstanceOf(Sponsor.class, dao.checkLogin("sponsor", "Sponsor@123"));
            assertEquals(sponsorId, sponsor.getId());
            assertEquals("0101234567", sponsor.getTaxCode());
            assertNull(sponsor.getPassword());
            assertNull(dao.checkLogin("admin", "wrong"));
            assertNull(dao.checkLogin("admin' OR 1=1 --", "Admin@123"));
        }
    }

    @Test
    void daoSupportsCrudAndRejectsDuplicateTaxCode() throws Exception {
        try (SponsorDAO dao = new SponsorDAO()) {
            Sponsor created = dao.create(sponsor("0234567890"));
            assertTrue(dao.searchByName("test sponsor").stream().anyMatch(s -> s.getId().equals(created.getId())));
            assertThrows(IllegalArgumentException.class, () -> dao.create(sponsor("0234567890")));
            created.setName("Updated"); dao.update(created.getId(), created);
            assertEquals("Updated", dao.getById(created.getId()).getName());
            dao.delete(created.getId());
            assertThrows(IllegalArgumentException.class, () -> dao.getById(created.getId()));
        }
    }

    @Test
    void contractsPreventDeletionAndUnsignedContractsAreHidden() throws Exception {
        try (SponsorDAO dao = new SponsorDAO(); ContractDAO contracts = new ContractDAO();
             var connection = DriverManager.getConnection(URL, "sa", "")) {
            Sponsor created = dao.create(sponsor("0234567891"));
            try (var statement = connection.prepareStatement("insert into contract(sponsor_id,barcode) values (?,?)")) {
                statement.setInt(1, created.getId()); statement.setString(2, "HD-TEST"); statement.executeUpdate();
            }
            assertTrue(contracts.getSignedBySponsor(created.getId()).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> dao.delete(created.getId()));
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("update contract set signed_date=current_date where sponsor_id=" + created.getId());
            }
            assertEquals(1, contracts.getSignedBySponsor(created.getId()).size());
        }
    }

    @Test
    void failedSponsorWriteRollsBackMemberInsert() throws Exception {
        try (var connection = DriverManager.getConnection(URL, "sa", "");
             var statement = connection.createStatement(); SponsorDAO dao = new SponsorDAO()) {
            long before;
            try (var result = statement.executeQuery("select count(*) from member")) { result.next(); before = result.getLong(1); }
            statement.execute("alter table sponsor add constraint reject_rollback check (field <> 'ROLLBACK')");
            try {
                Sponsor input = sponsor("0234567892"); input.setField("ROLLBACK");
                assertThrows(SQLException.class, () -> dao.create(input));
            } finally { statement.execute("alter table sponsor drop constraint reject_rollback"); }
            try (var result = statement.executeQuery("select count(*) from member")) { result.next(); assertEquals(before, result.getLong(1)); }
        }
    }

    @Test
    void jspLoginCrudAndValidationWorkOnRealTomcat() throws Exception {
        HttpClient client = client();
        String token = token(get(client, "/login.jsp").body());
        assertEquals(302, post(client, "/do-login.jsp", "csrf=" + token + "&username=admin&password=Admin%40123").statusCode());
        assertEquals(200, get(client, "/organizer/organizer-home.jsp").statusCode());
        var form = get(client, "/organizer/sponsor-form.jsp");
        assertEquals(200, form.statusCode());
        String body = "csrf=" + token(form.body()) + "&name=Web+Sponsor&taxCode=0234567893&field=Sports&address=Hanoi&email=web%40example.com&tel=0901234567&status=ACTIVE";
        var saved = post(client, "/organizer/do-save-sponsor.jsp", body);
        assertEquals(302, saved.statusCode());
        var detail = get(client, saved.headers().firstValue("location").orElseThrow());
        assertEquals(200, detail.statusCode()); assertTrue(detail.body().contains("Web Sponsor"));
        var duplicate = post(client, "/organizer/do-save-sponsor.jsp", body);
        assertEquals(200, duplicate.statusCode()); assertTrue(duplicate.body().contains("Tax code already exists"));
        assertEquals(200, get(client, "/organizer/sponsor-management.jsp").statusCode());
    }

    @Test
    void sessionRoleCsrfAndProfileOwnershipAreEnforced() throws Exception {
        HttpClient client = client();
        assertEquals(302, get(client, "/organizer/sponsor-management.jsp").statusCode());
        assertEquals(403, post(client, "/do-login.jsp", "username=admin&password=Admin%40123").statusCode());
        String csrf = token(get(client, "/login.jsp").body());
        post(client, "/do-login.jsp", "csrf=" + csrf + "&username=sponsor&password=Sponsor%40123");
        assertEquals(403, get(client, "/organizer/sponsor-management.jsp").statusCode());
        assertEquals(403, get(client, "/%6frganizer/sponsor-management.jsp").statusCode());
        assertEquals(405, get(client, "/sponsor/do-update-profile.jsp").statusCode());
        assertEquals(200, get(client, "/sponsor/profile-form.jsp").statusCode());
        assertEquals(302, post(client, "/sponsor/do-update-profile.jsp", "csrf=" + csrf
                + "&id=1&username=hacked&password=hacked&status=INACTIVE&name=Profile+Updated&taxCode=0101234567"
                + "&field=Sports&address=Hanoi&email=sponsor%40example.com&tel=0901234567").statusCode());
        try (SponsorDAO dao = new SponsorDAO(); MemberDAO members = new MemberDAO()) {
            Sponsor profile = dao.getById(sponsorId);
            assertEquals("Profile Updated", profile.getName());
            assertEquals(CooperationStatus.ACTIVE, profile.getStatus());
            assertNotNull(members.checkLogin("sponsor", "Sponsor@123"));
        }
        assertEquals(200, get(client, "/sponsor/profile.jsp").statusCode());
        assertEquals(302, post(client, "/do-logout.jsp", "csrf=" + csrf).statusCode());
        assertEquals(302, get(client, "/sponsor/profile.jsp").statusCode());
    }

    private static Sponsor sponsor(String taxCode) {
        Sponsor sponsor = new Sponsor(); sponsor.setName("Test Sponsor"); sponsor.setTaxCode(taxCode);
        sponsor.setField("Sports"); sponsor.setAddress("Hanoi"); sponsor.setEmail("test@example.com"); sponsor.setTel("0901234567");
        return sponsor;
    }

    private static int account(String username, String password) throws Exception {
        try (var connection = DriverManager.getConnection(URL, "sa", "");
             var statement = connection.prepareStatement("insert into member(username,password) values (?,?)", new String[]{"id"})) {
            statement.setString(1, username); statement.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(4)));
            statement.executeUpdate();
            try (var result = statement.getGeneratedKeys()) { result.next(); return result.getInt(1); }
        }
    }

    private static HttpClient client() {
        CookieManager cookies = new CookieManager(); cookies.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    private static HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(HttpClient client, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String token(String html) {
        var matcher = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), "Missing CSRF field");
        return URLEncoder.encode(matcher.group(1), StandardCharsets.UTF_8);
    }
}
