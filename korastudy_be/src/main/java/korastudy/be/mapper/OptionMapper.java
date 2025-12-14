package korastudy.be.mapper;

import korastudy.be.dto.response.quiz.OptionDTO;
import korastudy.be.entity.Course.Option;

import java.util.List;
import java.util.stream.Collectors;

public class OptionMapper {

    // ==================== SINGLE MAPPING ====================

    /**
     * Map Option → OptionDTO (cho Teacher/Admin - có đáp án)
     */
    public static OptionDTO toDTO(Option option) {
        if (option == null) return null;

        return OptionDTO.builder().id(option.getId()).optionText(option.getOptionText()).isCorrect(option.getIsCorrect()).orderIndex(option.getOrderIndex()).build();
    }

    /**
     * Map Option → OptionDTO (cho Student - ẩn đáp án)
     */
    public static OptionDTO toDTOForStudent(Option option) {
        if (option == null) return null;

        return OptionDTO.builder().id(option.getId()).optionText(option.getOptionText())
                // ⭐ KHÔNG set isCorrect cho student
                .orderIndex(option.getOrderIndex()).build();
    }

    // ==================== LIST MAPPING ====================

    /**
     * Map List<Option> → List<OptionDTO> (cho Teacher/Admin)
     */
    public static List<OptionDTO> toDTOs(List<Option> options) {
        if (options == null) return null;

        return options.stream().map(OptionMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Map List<Option> → List<OptionDTO> (cho Student)
     */
    public static List<OptionDTO> toDTOsForStudent(List<Option> options) {
        if (options == null) return null;

        return options.stream().map(OptionMapper::toDTOForStudent).collect(Collectors.toList());
    }
}