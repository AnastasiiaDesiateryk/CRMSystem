package org.example.crm.application.port.out;



public interface TokenHashing {
    String sha256Hex(String value);
}
