package com.ngtukien.demo.dao;

import com.ngtukien.demo.model.Contract;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContractDAO extends DAO {
    public ContractDAO() throws SQLException {
        super();
    }

    public List<Contract> getSignedBySponsor(int sponsorId) throws SQLException {
        try (var statement = con.prepareStatement("select * from contract where sponsor_id=? and signed_date is not null order by signed_date desc")) {
            statement.setInt(1, sponsorId);
            try (var result = statement.executeQuery()) {
                List<Contract> contracts = new ArrayList<>();
                while (result.next()) {
                    Contract contract = new Contract();
                    contract.setId(result.getInt("id"));
                    contract.setBarcode(result.getString("barcode"));
                    contract.setSignedDate(result.getObject("signed_date", LocalDate.class));
                    contract.setEffectiveFrom(result.getObject("effective_from", LocalDate.class));
                    contract.setEffectiveTo(result.getObject("effective_to", LocalDate.class));
                    contract.setStatus(result.getString("status"));
                    contracts.add(contract);
                }
                return contracts;
            }
        }
    }
}
