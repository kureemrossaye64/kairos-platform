package com.kairos.sports_atlas.governance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kairos.agentic.governance.InsightModule;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.notification.NotificationService;
import com.kairos.sports_atlas.logging.service.ChatLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableScheduling // Enable scheduling for the application
public class InsightGenerationService {

    private final List<InsightModule> insightModules;
    private final ChatLogService chatLogService;
    private final NotificationService notificationService;
    private final ChatLanguageModel chatLanguageModel;

    @Scheduled(cron = "0 0 5 * * *") // Run every day at 5 AM
    public void generateDailyReport() {
        log.info("Starting daily insight generation task...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String logs = chatLogService.getLogsForAnalysis(yesterday);
        
        if (logs.isBlank()) {
            log.info("No logs found for analysis for date {}. Skipping report.", yesterday);
            return;
        }

        StringBuilder reportBody = new StringBuilder("Kaya Daily Insight Report for " + yesterday + "\n\n");

        for (InsightModule module : insightModules) {
            log.info("Running analysis module: {}", module.getModuleName());
            // LangChain4j doesn't have a direct method with a system and user prompt,
            // so we combine them for this non-conversational task.
            String fullPrompt = module.getSystemPromptForAnalysis() + "\n\n--- CHAT LOGS ---\n" + logs;
            String analysis = chatLanguageModel.chat(fullPrompt);
            String formattedResult = module.processAnalysisResult(analysis);

            reportBody.append("--- " + module.getModuleName().toUpperCase() + " ---\n");
            reportBody.append(formattedResult).append("\n\n");
        }

        // We also need the demo implementation of the NotificationService
        notificationService.send("ministry-official@gov.mu", "Kaya Daily Insight Report", reportBody.toString());
        log.info("Daily insight report generated and sent.");
    }
}