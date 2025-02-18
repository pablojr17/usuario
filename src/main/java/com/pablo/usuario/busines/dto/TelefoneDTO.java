package com.pablo.usuario.busines.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefoneDTO {
    private String numero;
    private String ddd;
}
