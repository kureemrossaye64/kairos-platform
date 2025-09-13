package com.kairos.sports_atlas.service.ingestion;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kairos.ingestion.pipeline.DataSource;
import com.kairos.ingestion.pipeline.Pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SportsIngestionService {

    private final CsvParserProcessor csvParserProcessor;
    private final PerformanceRecordSink performanceRecordSink;

    /**
     * Processes an uploaded CSV file of performance results asynchronously.
     * @param csvFile The CSV file uploaded by the user.
     */
    @Async
    public void ingestPerformanceData(MultipartFile csvFile) {
        log.info("Starting CSV ingestion for file: {}", csvFile.getOriginalFilename());

        DataSource<InputStream> csvSource = () -> {
            try {
                return Stream.of(csvFile.getInputStream());
            } catch (IOException e) {
                log.error("Failed to get input stream from CSV file", e);
                return Stream.empty();
            }
        };

        // Build and Run the Pipeline
        Pipeline.from(csvSource)
                .through(csvParserProcessor) // InputStream -> PerformanceDataDto
                .to(performanceRecordSink);  // Consumes PerformanceDataDto
    }
}