package it.epicode.socialnetwork2.payloads;

// Il client salva accessToken e lo manda in ogni richiesta: Authorization: Bearer <token>
public record LoginResponse(String accessToken) {
}
