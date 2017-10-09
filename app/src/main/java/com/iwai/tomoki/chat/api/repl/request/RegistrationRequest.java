package com.iwai.tomoki.chat.api.repl.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration API Request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    public String botId;
}
