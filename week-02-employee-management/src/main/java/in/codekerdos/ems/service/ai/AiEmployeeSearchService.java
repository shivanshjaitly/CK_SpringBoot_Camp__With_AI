package in.codekerdos.ems.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.codekerdos.ems.dto.EmployeeSearchCriteria;
import in.codekerdos.ems.dto.NaturalLanguageSearchRequest;
import in.codekerdos.ems.dto.NaturalLanguageSearchResponse;
import in.codekerdos.ems.dto.PagedEmployeeResponse;
import in.codekerdos.ems.service.EmployeeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiEmployeeSearchService {

    private static final String SEARCH_PROMPT = """
            You convert HR natural language search queries into JSON filter criteria.
            Return ONLY valid JSON with these optional fields (use null if not mentioned):
            {
              "roleContains": "string or null",
              "team": "string or null",
              "departmentName": "string or null",
              "joinedAfter": "YYYY-MM-DD or null",
              "joinedBefore": "YYYY-MM-DD or null"
            }

            Examples:
            - "senior backend engineers" → roleContains: "Senior", team: "Backend"
            - "joined after 2022" → joinedAfter: "2022-01-01"
            - "Engineering department" → departmentName: "Engineering"

            Query: %s
            """;

    private final ChatClient chatClient;
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;

    public AiEmployeeSearchService(ChatClient.Builder builder, EmployeeService employeeService) {
        this.chatClient = builder.build();
        this.employeeService = employeeService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public NaturalLanguageSearchResponse search(NaturalLanguageSearchRequest request) {
        String rawJson = chatClient
                .prompt()
                .user(SEARCH_PROMPT.formatted(request.query()))
                .call()
                .content();

        EmployeeSearchCriteria criteria = parseCriteria(rawJson);

        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 10;

        PagedEmployeeResponse results = employeeService.searchByCriteria(criteria, page, size);

        return new NaturalLanguageSearchResponse(request.query(), criteria, results);
    }

    private EmployeeSearchCriteria parseCriteria(String rawJson) {
        try {
            String cleaned = rawJson
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            return objectMapper.readValue(cleaned, EmployeeSearchCriteria.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not parse AI search criteria: " + rawJson, ex);
        }
    }
}
