package com.cbp.testgen.report;

import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.mutation.Mutant;
import com.cbp.testgen.service.PipelineService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HtmlReportGenerator {

    public String generateHtmlReport(PipelineService.PipelineExecutionResult result) {
        StringBuilder sb = new StringBuilder();
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n<title>Test Generation & Effectiveness Report - ").append(result.getClassInfo().getClassName()).append("</title>\n");
        sb.append("<style>\n");
        sb.append("  :root { --primary: #2563eb; --primary-light: #dbeafe; --success: #16a34a; --danger: #dc2626; --warning: #d97706; --bg: #f8fafc; --card: #ffffff; --text: #1e293b; --muted: #64748b; }\n");
        sb.append("  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }\n");
        sb.append("  body { background: var(--bg); color: var(--text); padding: 30px; line-height: 1.5; }\n");
        sb.append("  .container { max-width: 1200px; margin: 0 auto; }\n");
        sb.append("  .header { background: #0f172a; color: white; padding: 25px; border-radius: 12px; margin-bottom: 25px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }\n");
        sb.append("  .header h1 { font-size: 24px; font-weight: 700; margin-bottom: 8px; }\n");
        sb.append("  .header p { color: #94a3b8; font-size: 14px; }\n");
        sb.append("  .banner-framing { background: #eff6ff; border-left: 4px solid var(--primary); padding: 15px; border-radius: 6px; margin-bottom: 25px; font-size: 14px; color: #1e40af; }\n");
        sb.append("  .grid-kpi { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 25px; }\n");
        sb.append("  .kpi-card { background: var(--card); padding: 20px; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); text-align: center; }\n");
        sb.append("  .kpi-val { font-size: 28px; font-weight: 800; margin: 5px 0; }\n");
        sb.append("  .kpi-val.green { color: var(--success); }\n");
        sb.append("  .kpi-val.blue { color: var(--primary); }\n");
        sb.append("  .kpi-val.amber { color: var(--warning); }\n");
        sb.append("  .kpi-label { font-size: 12px; font-weight: 600; text-transform: uppercase; color: var(--muted); letter-spacing: 0.5px; }\n");
        sb.append("  .section { background: var(--card); border-radius: 10px; border: 1px solid #e2e8f0; padding: 20px; margin-bottom: 25px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }\n");
        sb.append("  .section h2 { font-size: 18px; margin-bottom: 15px; color: #0f172a; border-bottom: 2px solid #f1f5f9; padding-bottom: 10px; }\n");
        sb.append("  table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }\n");
        sb.append("  th { background: #f8fafc; text-align: left; padding: 10px 12px; font-weight: 600; color: var(--muted); border-bottom: 2px solid #e2e8f0; }\n");
        sb.append("  td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; }\n");
        sb.append("  .badge { display: inline-block; padding: 3px 8px; border-radius: 9999px; font-size: 11px; font-weight: 600; text-transform: uppercase; }\n");
        sb.append("  .badge-pass { background: #dcfce7; color: #15803d; }\n");
        sb.append("  .badge-fail { background: #fee2e2; color: #b91c1c; }\n");
        sb.append("  .badge-killed { background: #dcfce7; color: #15803d; }\n");
        sb.append("  .badge-survived { background: #fee2e2; color: #b91c1c; }\n");
        sb.append("  .badge-weak { background: #fef3c7; color: #b45309; }\n");
        sb.append("  .code-block { background: #0f172a; color: #e2e8f0; padding: 15px; border-radius: 8px; font-family: 'Courier New', monospace; font-size: 12px; overflow-x: auto; white-space: pre-wrap; }\n");
        sb.append("  .footer { text-align: center; color: var(--muted); font-size: 12px; margin-top: 40px; padding-top: 20px; border-top: 1px solid #e2e8f0; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<div class=\"container\">\n");
        sb.append("  <div class=\"header\">\n");
        sb.append("    <h1>Intelligent Java Test Case Generator — Analysis Report</h1>\n");
        sb.append("    <p>Target Class: <strong>").append(result.getClassInfo().getFullClassName()).append("</strong> | Generated at: ").append(timeStr).append("</p>\n");
        sb.append("  </div>\n");

        sb.append("  <div class=\"banner-framing\">\n");
        sb.append("    <strong>Project Philosophy:</strong> <em>\"Existing tools generate tests and forget them; this tool generates tests, measures how effective they really are through mutation analysis, stores results in a relational database, and intelligently prioritizes re-testing across code versions.\"</em>\n");
        sb.append("  </div>\n");

        // KPI Cards
        sb.append("  <div class=\"grid-kpi\">\n");
        sb.append("    <div class=\"kpi-card\"><div class=\"kpi-label\">Mutation Score</div><div class=\"kpi-val green\">").append(result.getMutationSummary().getMutationScorePct()).append("%</div><div style=\"font-size: 11px; color: var(--muted);\">").append(result.getMutationSummary().getMutantsKilled()).append(" of ").append(result.getMutationSummary().getTotalMutants()).append(" mutants killed</div></div>\n");
        sb.append("    <div class=\"kpi-card\"><div class=\"kpi-label\">Line Coverage</div><div class=\"kpi-val blue\">").append(result.getCoverageSummary().getLineCoveragePct()).append("%</div><div style=\"font-size: 11px; color: var(--muted);\">").append(result.getCoverageSummary().getCoveredLines()).append(" of ").append(result.getCoverageSummary().getTotalLines()).append(" lines</div></div>\n");
        sb.append("    <div class=\"kpi-card\"><div class=\"kpi-label\">Total Tests Run</div><div class=\"kpi-val blue\">").append(result.getExecutionSummary().getTotalCount()).append("</div><div style=\"font-size: 11px; color: var(--muted);\">Pass: ").append(result.getExecutionSummary().getPassedCount()).append(", Fail: ").append(result.getExecutionSummary().getFailedCount()).append("</div></div>\n");
        sb.append("    <div class=\"kpi-card\"><div class=\"kpi-label\">Suite Minimization</div><div class=\"kpi-val amber\">-").append(result.getMinimizationReport().getReductionPercentage()).append("%</div><div style=\"font-size: 11px; color: var(--muted);\">").append(result.getMinimizationReport().getInitialTestCount()).append(" &rarr; ").append(result.getMinimizationReport().getMinimizedTestCount()).append(" tests</div></div>\n");
        sb.append("  </div>\n");

        // Mutation Analysis Table
        sb.append("  <div class=\"section\">\n");
        sb.append("    <h2>AST Mutation Analysis (Defect Detection Power)</h2>\n");
        sb.append("    <p style=\"font-size: 13px; color: var(--muted); margin-bottom: 10px;\">Mutation testing deliberately introduces subtle defects (mutants) into the source code to prove whether generated tests actually detect them.</p>\n");
        sb.append("    <table>\n");
        sb.append("      <thead><tr><th>Mutant ID</th><th>Method</th><th>Line</th><th>Mutation Operator</th><th>Original &rarr; Mutated</th><th>Status</th><th>Killing Test</th></tr></thead>\n");
        sb.append("      <tbody>\n");
        for (Mutant m : result.getMutationSummary().getMutants()) {
            sb.append("        <tr>\n");
            sb.append("          <td><code>").append(m.getId()).append("</code></td>\n");
            sb.append("          <td>").append(m.getMethodName()).append("</td>\n");
            sb.append("          <td>").append(m.getLineNumber()).append("</td>\n");
            sb.append("          <td>").append(m.getOperator().getDisplayName()).append("</td>\n");
            sb.append("          <td><code>").append(escapeHtml(m.getOriginalSnippet())).append("</code> &rarr; <code>").append(escapeHtml(m.getMutatedSnippet())).append("</code></td>\n");
            sb.append("          <td><span class=\"badge ").append(m.isKilled() ? "badge-killed" : "badge-survived").append("\">").append(m.getStatus()).append("</span></td>\n");
            sb.append("          <td>").append(m.getKillingTestName() != null ? "<code>" + m.getKillingTestName() + "</code>" : "<span style=\"color:var(--muted);\">None</span>").append("</td>\n");
            sb.append("        </tr>\n");
        }
        sb.append("      </tbody>\n");
        sb.append("    </table>\n");
        sb.append("  </div>\n");

        // Generated Test Cases Table
        sb.append("  <div class=\"section\">\n");
        sb.append("    <h2>Generated JUnit 5 Test Suite Details</h2>\n");
        sb.append("    <table>\n");
        sb.append("      <thead><tr><th>Test Method Name</th><th>Category</th><th>Method</th><th>Assertions</th><th>Quality</th><th>Description</th></tr></thead>\n");
        sb.append("      <tbody>\n");
        for (JUnit5TestGenerator.GeneratedTestCaseData tc : result.getTestSuite().getTestCases()) {
            sb.append("        <tr>\n");
            sb.append("          <td><code>").append(tc.getTestName()).append("()</code></td>\n");
            sb.append("          <td><span class=\"badge\" style=\"background:#e0e7ff;color:#3730a3;\">").append(tc.getTestType()).append("</span></td>\n");
            sb.append("          <td>").append(tc.getMethodName()).append("</td>\n");
            sb.append("          <td>").append(tc.getAssertionCount()).append("</td>\n");
            sb.append("          <td><span class=\"badge ").append(tc.isWeak() ? "badge-weak" : "badge-pass").append("\">").append(tc.isWeak() ? "WEAK" : "VALID").append("</span></td>\n");
            sb.append("          <td>").append(tc.getDescription()).append("</td>\n");
            sb.append("        </tr>\n");
        }
        sb.append("      </tbody>\n");
        sb.append("    </table>\n");
        sb.append("  </div>\n");

        // Full Generated Code View
        sb.append("  <div class=\"section\">\n");
        sb.append("    <h2>Generated Test Suite Source Code (").append(result.getTestSuite().getTestClassName()).append(".java)</h2>\n");
        sb.append("    <pre class=\"code-block\">").append(escapeHtml(result.getTestSuite().getFullSourceCode())).append("</pre>\n");
        sb.append("  </div>\n");

        sb.append("  <div class=\"footer\">\n");
        sb.append("    <p>Curricular Based Project (CBP) — Software Engineering &bull; DBMS 3NF &bull; Object-Oriented Design &bull; Java 21 LTS</p>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n</body>\n</html>");

        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
