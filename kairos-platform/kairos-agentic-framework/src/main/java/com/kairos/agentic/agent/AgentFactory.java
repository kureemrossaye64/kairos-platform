package com.kairos.agentic.agent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.kairos.agentic.conversational.ConversationalIngestionTool;
import com.kairos.agentic.tools.KairosTool;
import com.kairos.agentic.tools.ToolSpecifications;
import com.kairos.agentic.transactional.TransactionalTool;
import com.kairos.core.agentic.ConversationContexProvider;
import com.kairos.core.ai.ChatLanguageModel;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A factory for creating AiAgent instances.
 * This class encapsulates the logic of discovering tools and wiring them into a
 * LangChain4j AiService.
 */
@RequiredArgsConstructor
@Slf4j
public class AgentFactory {

    private final ApplicationContext context; // Used to discover @KairosTool beans
    private final ChatLanguageModel chatLanguageModel;
    private final ConversationContexProvider contextProvider;
    
   
    
    /**
     * Creates a new conversational agent with a specific system prompt and all available tools.
     * @param systemPrompt The system prompt that defines the agent's persona and mission.
     * @return A fully configured AiAgent instance.
     */
    public AiAgent createAgent(String systemPrompt, String sessionId) {
        Collection<Object> tools_ = context.getBeansWithAnnotation(KairosTool.class).values();
        var chatMemory = contextProvider.getChatMemory(sessionId);
        Map<ToolSpecification, ToolExecutor> tool = new ToolSpecifications(context).getSpecifications();
        
        List<Object> tools = new ArrayList<Object>();
        if(tool.isEmpty()) {
        	for(Object t : tools_) {
        		if( t instanceof ConversationalIngestionTool) {
        			continue;
        		}
        		
        		if(t instanceof TransactionalTool) {
        			continue;
        		}
        		tools.add(t);
        	}
        	
        }else {
        	tools.addAll(tools_);
        }
         
       
        
       /* if (isAreEnabled && guardianAgent != null) {
            log.warn("Axiom-Driven Reasoning Engine (ARE) is ENABLED. Building Multi-Agent Council.");

            // 1. Gather all tools for the Investigative Agent.
            //Collection<Object> staticTools = context.getBeansWithAnnotation(KairosTool.class).values();
            //List<Object> allTools = new ArrayList<>(staticTools);
            // We can add dynamically discovered tools here in the future if needed.
            
            // 2. Build the Investigative Agent AiService.
            // It has its own, temporary memory for each investigation.
            InvestigativeAgent investigativeAgent = AiServices.builder(InvestigativeAgent.class)
                    .chatModel(chatLanguageModel)
                    .tools(tool)
                    .tools(tools.toArray())
                    .chatMemoryProvider(session -> MessageWindowChatMemory.withMaxMessages(5))
                    .build();

            // 3. Build the Executive Agent and inject its council members.
            return new AxiomDrivenExecutiveAgent(
                    chatLanguageModel,
                    chatMemory,
                    systemPrompt, // Pass the main "Kaya" persona here
                    investigativeAgent,
                    guardianAgent
            );

        }*/
        
        
        
        return AiServices.builder(AiAgent.class)
                .chatModel(chatLanguageModel.getModel())
                .systemMessageProvider((o)->{ return systemPrompt;})
                .tools(tool)
                .tools(tools.toArray())
                .chatMemory(chatMemory)
                .chatMemoryProvider(null)
                .build();
    }
    
     
}