package com.raf.mrworldwide.domain.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toCollection;

@Converter
public class CsvConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attributes) {
        if (Optional.ofNullable(attributes).isEmpty()) return "";
        return String.join(",", attributes);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (Optional.ofNullable(dbData).filter(s -> !s.isEmpty()).isEmpty()) return new ArrayList<>();
        return Arrays.stream(dbData.split(",")).collect(toCollection(ArrayList::new));
    }

}
