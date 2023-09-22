package com.Bridge.bridge.dto.request;

import com.Bridge.bridge.domain.Field;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserFieldRequest {

    private List<String> fieldName;

    public List<Field> toEntity() {
        return fieldName.stream()
                .map(Field::new)
                .collect(Collectors.toList());
    }
}