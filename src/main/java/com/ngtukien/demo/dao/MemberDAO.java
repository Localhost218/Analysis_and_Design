package com.ngtukien.demo.dao;

import com.ngtukien.demo.model.Member;
import com.ngtukien.demo.model.Organizer;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class MemberDAO extends DAO {
    public MemberDAO() throws SQLException {
        super();
    }

    public Member checkLogin(String username, String password) throws SQLException {
        try (var statement = con.prepareStatement("""
                select m.id, m.username, m.password, o.id as organizer_id, o.full_name,
                o.address as organizer_address, o.date_of_birth, o.email as organizer_email,
                s.name, s.tax_code, s.field, s.address, s.email, s.tel, s.status
                from member m left join organizer o on o.id=m.id left join sponsor s on s.id=m.id
                where m.username=? and (o.id is not null or s.id is not null)
                """)) {
            statement.setString(1, username);
            try (var result = statement.executeQuery()) {
                if (!result.next()) return null;
                String hash = result.getString("password");
                try {
                    if (hash == null || !BCrypt.checkpw(password, hash)) return null;
                } catch (IllegalArgumentException exception) {
                    return null;
                }
                Member member;
                if (result.getObject("organizer_id") != null) {
                    Organizer organizer = new Organizer();
                    organizer.setFullName(result.getString("full_name"));
                    organizer.setAddress(result.getString("organizer_address"));
                    organizer.setDateOfBirth(result.getObject("date_of_birth", LocalDateTime.class));
                    organizer.setEmail(result.getString("organizer_email"));
                    member = organizer;
                } else {
                    member = SponsorDAO.mapSponsor(result);
                }
                member.setId(result.getInt("id"));
                member.setUsername(result.getString("username"));
                member.setPassword(null);
                return member;
            }
        }
    }
}
