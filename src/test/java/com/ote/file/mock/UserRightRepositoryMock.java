package com.ote.file.mock;

import com.ote.file.spi.IUserRightRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserRightRepositoryMock implements IUserRightRepository {

    private final Map<Key, Set<Privilege>> rights = new HashMap<>();

    @Override
    public Set<Privilege> getPrivileges(String user, String application, String perimeter) {
        Key key = new Key(user, application, perimeter);
        return getPrivileges(key);
    }

    private Set<Privilege> getPrivileges(Key key) {
        return rights.entrySet().stream().
                filter(p -> p.getKey().equals(key)).
                map(p -> p.getValue()).
                findAny().
                orElse(new HashSet<>());
    }

    public void addPrivilege(String user, String application, String perimeter, Privilege privilege) {
        Key key = new Key(user, application, perimeter);
        Set<Privilege> privileges = getPrivileges(key);
        privileges.add(privilege);
        rights.put(key, privileges);
    }

    public void reset() {
        rights.clear();
    }

    @Data
    @RequiredArgsConstructor
    public static class Key {
        private final String user;
        private final String application;
        private final String perimeter;
    }
}
