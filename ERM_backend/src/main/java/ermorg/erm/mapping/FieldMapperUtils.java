package ermorg.erm.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.UserDto;
import ermorg.erm.model.Department;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IUserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FieldMapperUtils {

    private final IUserService userService;
    private final DepartmentRepository departmentRepository;

    public String resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userService.getUserNameOrEmail(userId);
    }

    public String resolveUserFromObject(Object value) {
        Long resolvedId = parseNullableLong(value);
        if (resolvedId != null) {
            return resolveUser(resolvedId);
        }
        return value != null ? value.toString() : null;
    }

    public String resolveDepartmentFromObject(Object value) {
        Long resolvedId = parseNullableLong(value);
        if (resolvedId == null) {
            return value != null ? value.toString() : null;
        }
        return departmentRepository.findById(resolvedId)
                .filter(department -> !Boolean.TRUE.equals(department.getDeleted()))
                .map(Department::getName)
                .orElse(value.toString());
    }

    public Long parseNullableLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String text = ((String) value).trim();
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    /**
     * If {@code value} already looks like a rating text label (contains letters),
     * return it as-is. If it is a pure numeric string (frontend sent a score
     * instead of a label), convert it to the standard label using the same
     * 5-band scale the UI uses. Returns null when value is null/blank.
     */
    public String resolveRatingLabel(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        if (text.isEmpty()) return null;

        // Already a text label — return unchanged
        if (!text.matches("-?\\d+(\\.\\d+)?")) {
            return text;
        }

        // Numeric score → label
        try {
            double score = Double.parseDouble(text);
            if (score >= 20) return "Critical";
            if (score >= 15) return "Very High";
            if (score >= 10) return "High";
            if (score >= 5)  return "Medium";
            return "Low";
        } catch (NumberFormatException e) {
            return text;
        }
    }

    public String formatEnum(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        String normalized = text.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("_+", "_").toUpperCase();
        if (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    public List<String> resolveSubList(Object value) {
        if (!(value instanceof Collection<?>)) {
            return Collections.emptyList();
        }

        Collection<?> collection = (Collection<?>) value;
        List<String> result = new ArrayList<>();
        for (Object element : collection) {
            String listValue = resolveListElement(element);
            if (listValue != null && !listValue.trim().isEmpty()) {
                result.add(listValue);
            }
        }
        return result;
    }

    public String stringify(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            return collection.stream()
                    .map(Objects::toString)
                    .filter(item -> item != null && !item.trim().isEmpty())
                    .collect(java.util.stream.Collectors.joining(", "));
        }

        return value.toString();
    }

    private String resolveListElement(Object element) {
        if (element == null) {
            return null;
        }

        if (element instanceof String || element instanceof Number || element instanceof Boolean) {
            return element.toString();
        }

        if (element instanceof Enum<?>) {
            return ((Enum<?>) element).name();
        }

        if (element instanceof UserDto) {
            return ((UserDto) element).getName();
        }

        return element.toString();
    }
}
