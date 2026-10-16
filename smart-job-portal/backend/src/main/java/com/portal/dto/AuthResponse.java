package com.portal.dto;

import com.portal.entity.Role;

public record AuthResponse(String token, String name, String email, Role role) {}
