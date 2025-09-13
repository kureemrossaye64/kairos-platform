package com.kairos.sports_atlas.service.ingestion;

import com.kairos.ingestion.pipeline.Processor;
import com.kairos.sports_atlas.facility.dto.PerformanceDataDto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A Processor that parses an InputStream (expected to be a CSV file)
 * and transforms it into a stream of PerformanceDataDto objects.
 */
@Component
@Slf4j
public class CsvParserProcessor implements Processor<InputStream, PerformanceDataDto> {

    // Expects CSV with headers: Name,DateOfBirth,Sport,Event,Result,Unit,EventDate
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public Stream<PerformanceDataDto> process(Stream<InputStream> inputStream) {
        return inputStream.flatMap(is -> {
            try {
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                CSVParser parser = new CSVParser(reader, CSV_FORMAT);
                
                // Convert the CSVRecord iterator to a parallel stream for processing
                return StreamSupport.stream(parser.spliterator(), false)
                    .map(this::recordToDto)
                    .filter(java.util.Objects::nonNull);
            } catch (Exception e) {
                log.error("Failed to parse CSV stream", e);
                return Stream.empty();
            }
        });
    }

    private PerformanceDataDto recordToDto(CSVRecord record) {
        try {
            return PerformanceDataDto.builder()
                .athleteName(record.get("Name"))
                .dateOfBirth(LocalDate.parse(record.get("DateOfBirth"), DATE_FORMATTER))
                .sport(record.get("Sport"))
                .eventName(record.get("Event"))
                .result(record.get("Result"))
                .unit(record.get("Unit"))
                .eventDate(LocalDate.parse(record.get("EventDate"), DATE_FORMATTER))
                .build();
        } catch (Exception e) {
            log.warn("Skipping invalid CSV record #{}: {}", record.getRecordNumber(), e.getMessage());
            return null;
        }
    }
}