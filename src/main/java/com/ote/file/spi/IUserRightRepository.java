package com.ote.file.spi;

import java.util.Set;

public interface IUserRightRepository {

    Set<Privilege> getPrivileges(String user, String application, String perimeter);

    default boolean isAuthorized(String user, String application, String perimeter, Privilege privilege) {

        Set<Privilege> privileges = getPrivileges(user, application, perimeter);

        switch (privilege) {
            case READ:
                return privileges.stream().
                        anyMatch(p -> p.equals(Privilege.WRITE) || p.equals(Privilege.READ));
            case WRITE:
                return privileges.stream().
                        anyMatch(p -> p.equals(Privilege.WRITE));
        }
        return false;
    }

    enum Privilege {

        WRITE, READ;

        public String getAction() {
            return this.toString().toLowerCase();
        }
    }
}
