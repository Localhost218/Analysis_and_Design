package com.ngtukien.demo.dao;

import com.ngtukien.demo.model.Sponsor;
import com.ngtukien.demo.model.enums.CooperationStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SponsorDAO extends DAO {
    public SponsorDAO() throws SQLException {
        super();
    }

    static Sponsor mapSponsor(ResultSet result) throws SQLException {
        Sponsor sponsor = new Sponsor();
        sponsor.setId(result.getInt("id"));
        sponsor.setName(result.getString("name"));
        sponsor.setTaxCode(result.getString("tax_code"));
        sponsor.setField(result.getString("field"));
        sponsor.setAddress(result.getString("address"));
        sponsor.setEmail(result.getString("email"));
        sponsor.setTel(result.getString("tel"));
        sponsor.setStatus(CooperationStatus.valueOf(result.getString("status")));
        return sponsor;
    }

    public List<Sponsor> searchByName(String keyword) throws SQLException {
        String escaped = keyword.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        try (var statement = con.prepareStatement("select * from sponsor where lower(name) like ? escape '!' order by name")) {
            statement.setString(1, "%" + escaped + "%");
            try (var result = statement.executeQuery()) {
                List<Sponsor> sponsors = new ArrayList<>();
                while (result.next()) sponsors.add(mapSponsor(result));
                return sponsors;
            }
        }
    }

    public Sponsor getById(int id) throws SQLException {
        try (var statement = con.prepareStatement("select * from sponsor where id=?")) {
            statement.setInt(1, id);
            try (var result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Sponsor was not found");
                return mapSponsor(result);
            }
        }
    }

    public Sponsor create(Sponsor sponsor) throws SQLException {
        validate(sponsor);
        int id = transaction(() -> {
            validateTaxCode(sponsor.getTaxCode(), null);
            int newId;
            try (var statement = con.prepareStatement("insert into member(username,password) values (null,null)", new String[]{"id"})) {
                statement.executeUpdate();
                try (var keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Unable to retrieve the sponsor ID");
                    newId = keys.getInt(1);
                }
            }
            write(sponsor, newId, true);
            return newId;
        });
        sponsor.setId(id);
        return sponsor;
    }

    public void update(int id, Sponsor input) throws SQLException {
        validate(input);
        transaction(() -> {
            getById(id);
            validateTaxCode(input.getTaxCode(), id);
            write(input, id, false);
            return null;
        });
    }

    public void updateProfile(int loggedInSponsorId, Sponsor input) throws SQLException {
        validate(input);
        transaction(() -> {
            getById(loggedInSponsorId);
            validateTaxCode(input.getTaxCode(), loggedInSponsorId);
            try (var statement = con.prepareStatement("""
                    update sponsor set name=?, tax_code=?, field=?, address=?, email=?, tel=? where id=?
                    """)) {
                statement.setString(1, input.getName().strip());
                statement.setString(2, input.getTaxCode().strip());
                statement.setString(3, input.getField().strip());
                statement.setString(4, input.getAddress().strip());
                statement.setString(5, input.getEmail().strip());
                statement.setString(6, input.getTel().strip());
                statement.setInt(7, loggedInSponsorId);
                if (statement.executeUpdate() != 1) throw new SQLException("Unable to update the profile");
            }
            return null;
        });
    }

    public void delete(int id) throws SQLException {
        transaction(() -> {
            getById(id);
            try (var statement = con.prepareStatement("select 1 from contract where sponsor_id=?")) {
                statement.setInt(1, id);
                try (var result = statement.executeQuery()) {
                    if (result.next()) throw new IllegalArgumentException("Cannot delete a sponsor with contracts. Update the cooperation status instead.");
                }
            }
            try (var statement = con.prepareStatement("delete from sponsor where id=?")) {
                statement.setInt(1, id); statement.executeUpdate();
            }
            try (var statement = con.prepareStatement("delete from member where id=?")) {
                statement.setInt(1, id); statement.executeUpdate();
            }
            return null;
        });
    }

    private void write(Sponsor sponsor, int id, boolean creating) throws SQLException {
        String sql = creating
                ? "insert into sponsor(name,tax_code,field,address,email,tel,status,id) values (?,?,?,?,?,?,?,?)"
                : "update sponsor set name=?,tax_code=?,field=?,address=?,email=?,tel=?,status=? where id=?";
        try (var statement = con.prepareStatement(sql)) {
            statement.setString(1, sponsor.getName().strip());
            statement.setString(2, sponsor.getTaxCode().strip());
            statement.setString(3, sponsor.getField().strip());
            statement.setString(4, sponsor.getAddress().strip());
            statement.setString(5, sponsor.getEmail().strip());
            statement.setString(6, sponsor.getTel().strip());
            statement.setString(7, sponsor.getStatus().name());
            statement.setInt(8, id);
            if (statement.executeUpdate() != 1) throw new SQLException("Unable to save the sponsor");
        }
    }

    private void validateTaxCode(String taxCode, Integer excludedId) throws SQLException {
        String sql = "select 1 from sponsor where tax_code=?" + (excludedId == null ? "" : " and id<>?");
        try (var statement = con.prepareStatement(sql)) {
            statement.setString(1, taxCode.strip());
            if (excludedId != null) statement.setInt(2, excludedId);
            try (var result = statement.executeQuery()) {
                if (result.next()) throw new IllegalArgumentException("Tax code already exists");
            }
        }
    }

    private void validate(Sponsor sponsor) {
        required(sponsor.getName(), 200, "Sponsor Name");
        required(sponsor.getTaxCode(), 14, "Tax Code");
        required(sponsor.getField(), 200, "Business Field");
        required(sponsor.getAddress(), 255, "Address");
        required(sponsor.getEmail(), 200, "Email");
        required(sponsor.getTel(), 20, "Phone Number");
        if (!sponsor.getTaxCode().strip().matches("[0-9]{10}(-[0-9]{3})?")) throw new IllegalArgumentException("Invalid tax code");
        if (!sponsor.getEmail().strip().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Invalid email address");
        if (!sponsor.getTel().strip().matches("[+0-9() .-]{9,20}")) throw new IllegalArgumentException("Invalid phone number");
        if (sponsor.getStatus() == null) throw new IllegalArgumentException("Select a cooperation status");
    }

    private void required(String value, int maxLength, String label) {
        if (value == null || value.isBlank() || value.strip().length() > maxLength) {
            throw new IllegalArgumentException(label + " is required and must not exceed " + maxLength + " characters");
        }
    }
}
