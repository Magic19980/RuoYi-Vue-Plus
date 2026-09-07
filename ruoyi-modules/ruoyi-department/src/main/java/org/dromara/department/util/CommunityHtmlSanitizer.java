package org.dromara.department.util;

import cn.hutool.http.HTMLFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 社区正文 HTML 清洗器。
 *
 * <p>社区正文由 Tiptap 生成，不能沿用全局的“删除所有 HTML 标签”策略，
 * 否则段落、图片、表格等结构会在保存前丢失。这里仅保留编辑器需要的标签和属性，
 * 并对内联样式做进一步限制。</p>
 */
public final class CommunityHtmlSanitizer {

    private static final Pattern STYLE_ATTRIBUTE = Pattern.compile("(?is)(\\sstyle\\s*=\\s*)([\\\"'])(.*?)\\2");
    private static final Pattern SAFE_STYLE_DECLARATION = Pattern.compile(
        "(?i)^(?:"
            + "(?:width|min-width)\\s*:\\s*(?:\\d+(?:\\.\\d+)?(?:px|%)|auto)"
            + "|height\\s*:\\s*auto"
            + "|float\\s*:\\s*(?:left|right|none)"
            + "|text-align\\s*:\\s*(?:left|center|right|justify)"
            + "|background-color\\s*:\\s*#[0-9a-f]{3,8}"
            + "|border-radius\\s*:\\s*\\d+(?:\\.\\d+)?px"
            + "|vertical-align\\s*:\\s*(?:top|middle|bottom)"
            + ")$");

    private CommunityHtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return new HTMLFilter(buildConfiguration()).filter(sanitizeStyles(html));
    }

    private static String sanitizeStyles(String html) {
        Matcher matcher = STYLE_ATTRIBUTE.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String safeStyle = sanitizeStyle(matcher.group(3));
            String replacement = safeStyle.isEmpty() ? "" : matcher.group(1) + matcher.group(2)
                + safeStyle + matcher.group(2);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String sanitizeStyle(String style) {
        return java.util.Arrays.stream(style.split(";"))
            .map(String::trim)
            .filter(declaration -> !declaration.isEmpty() && SAFE_STYLE_DECLARATION.matcher(declaration).matches())
            .reduce((left, right) -> left + "; " + right)
            .orElse("");
    }

    private static Map<String, Object> buildConfiguration() {
        Map<String, List<String>> allowedTags = new HashMap<>();
        allowedTags.put("a", List.of("href", "target", "rel", "title"));
        allowedTags.put("br", List.of());
        allowedTags.put("blockquote", List.of("class"));
        allowedTags.put("code", List.of("class"));
        allowedTags.put("div", List.of("class", "data-type"));
        allowedTags.put("em", List.of("class"));
        allowedTags.put("h1", List.of("class"));
        allowedTags.put("h2", List.of("class"));
        allowedTags.put("h3", List.of("class"));
        allowedTags.put("h4", List.of("class"));
        allowedTags.put("h5", List.of("class"));
        allowedTags.put("h6", List.of("class"));
        allowedTags.put("i", List.of("class"));
        allowedTags.put("img", List.of("src", "alt", "title", "width", "height", "class", "style",
            "data-align", "data-wrap"));
        allowedTags.put("li", List.of("class", "data-type", "data-checked"));
        allowedTags.put("mark", List.of("class", "data-color", "style"));
        allowedTags.put("ol", List.of("class", "data-type", "start"));
        allowedTags.put("p", List.of("class", "style"));
        allowedTags.put("pre", List.of("class"));
        allowedTags.put("s", List.of("class"));
        allowedTags.put("strong", List.of("class"));
        allowedTags.put("sub", List.of("class"));
        allowedTags.put("sup", List.of("class"));
        allowedTags.put("table", List.of("class", "style"));
        allowedTags.put("tbody", List.of("class"));
        allowedTags.put("td", tableCellAttributes());
        allowedTags.put("tfoot", List.of("class"));
        allowedTags.put("th", tableCellAttributes());
        allowedTags.put("thead", List.of("class"));
        allowedTags.put("tr", List.of("class"));
        allowedTags.put("u", List.of("class"));
        allowedTags.put("ul", List.of("class", "data-type"));
        allowedTags.put("hr", List.of("class"));

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("vAllowed", allowedTags);
        configuration.put("vSelfClosingTags", new String[]{"br", "hr", "img"});
        configuration.put("vNeedClosingTags", new String[]{
            "a", "blockquote", "code", "div", "em", "h1", "h2", "h3", "h4", "h5", "h6", "i",
            "li", "mark", "ol", "p", "pre", "s", "strong", "sub", "sup", "table", "tbody", "tfoot",
            "td", "th", "thead", "tr", "u", "ul"
        });
        configuration.put("vDisallowed", new String[]{
            "applet", "base", "button", "embed", "form", "iframe", "input", "link", "meta", "object",
            "script", "select", "style", "svg", "textarea", "title", "video", "audio"
        });
        configuration.put("vAllowedProtocols", new String[]{"http", "https", "mailto", "tel", "oss"});
        configuration.put("vProtocolAtts", new String[]{"href", "src"});
        configuration.put("vAllowedEntities", new String[]{"amp", "gt", "lt", "quot", "apos", "nbsp"});
        configuration.put("vRemoveBlanks", new String[]{});
        configuration.put("stripComment", Boolean.TRUE);
        configuration.put("alwaysMakeTags", Boolean.FALSE);
        return configuration;
    }

    private static List<String> tableCellAttributes() {
        return List.of("class", "style", "colspan", "rowspan", "colwidth", "align");
    }
}
