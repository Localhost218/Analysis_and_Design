package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Driver;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class DriverDAO extends MemberDAO {

    /** Tay đua kèm danh sách tài khoản ngân hàng (listBankAccount). */
    public Driver getDriverInfo(int driverId) {
        Driver driver = em.find(Driver.class, driverId);
        if (driver != null) {
            Hibernate.initialize(driver.getListBankAccount());
        }
        return driver;
    }
}
