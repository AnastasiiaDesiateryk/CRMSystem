package org.example.crm.adapter.in.web.controller.request;

import java.util.Set;

public record UpdateUserRolesRequest(Set<String> roles) {
}