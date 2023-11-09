package mxml.enums;

public enum TemplatePlaceholder {
    FORM_CONTENT("{form_content}"),
    CSS_FILE_NAME("{css_file_name}"),
    TITLE_FORM_CONTENT("{title_form_content}"),
    STYLE_COMPOSITION_FIRST("{style_composition_first}"),
    CLASS_FIRST_CONTENT("{class_first_content}"),
    START_FIRST_UI_DETAIL("{start_first_ui_detail}"),
    END_FIRST_UI_DETAIL("{end_first_ui_detail}");

    private final String placeholder;

    TemplatePlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}

