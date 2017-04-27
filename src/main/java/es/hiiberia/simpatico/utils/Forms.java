package es.hiiberia.simpatico.utils;

public class Forms {

	// Pieces of different form's divs
	public static String DIALOG_START = "<div id=\"dialog_modal_session_feedback_a\" class=\"modalligazon modal-session-feedback\">";
	public static String DIALOG_COMMON = 
			"<!-- Faces -->"+
			  "<div id=\"faces-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "¿Cree que en conjunto las herramientas de SIMPATICO le facilitaron la comprensión del servicio?"+
			    "</div>"+
			    "<div id=\"face-radio-buttons-session-feedback\" class=\"cc-selector\">"+
			      "<input id=\"face-happy-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"happy\"/>"+
			      "<label for=\"happy\" data-face=\"happy\" class=\"left\" style=\"margin-left: 10%;border-radius: 20%;\"><img src=\"img/happy_face.png\" alt=\"Happy\"/></label>"+
			      "<input id=\"face-normal-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"normal\"/>"+
			      "<label for=\"normal\" data-face=\"normal\" style=\"margin-left: 20%;border-radius: 20%;\"><img src=\"img/normal_face.png\" alt=\"Normal\"/></label>"+
			      "<input id=\"face-sad-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"sad\"/>"+
			      "<label for=\"sad\" data-face=\"sad\" class=\"right\" style=\"margin-right: 10%;border-radius: 20%;\"><img src=\"img/sad_face.png\" alt=\"Sad\" /></label>"+
			    "</div>"+
			  "</div>"+
			  "<!-- Comments -->"+
			  "<div id=\"comentarios-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "¿Tiene alguna sugerencia para los desarrolladores de SIMPATICO?"+
			    "</div>"+
			    "<div id=\"session-feedback-comments\" class=\"session-feedback-comments\">"+
			      "<textarea id=\"session-feedback-comments-text\" class=\"session-feedback-comments-text\" placeholder=\"Escriba su opinión...\" cols=\"40\" rows=\"5\" style=\"resize: none;\"></textarea>"+
			    "</div>"+
			  "</div>"+
			  "<!-- Buttons send/cancel -->"+
			  "<div id=\"buttons_session_feedback\" style=\"padding-top: 50px;padding-bottom: 20px\">"+
			    "<div class=\"mais button_cancel_session_feedback\" style=\"position:relative;float:left;margin-left:40px;\">"+
			      "<a id=\"button_cancel_session_feedback_text\" style=\"border-radius:5px 5px 5px 5px;width:auto;\">No quiero</a>"+
			    "</div>"+
			    "<div class=\"mais\" id=\"button_send_session_feedback_a\" style=\"position:relative;float:right;margin-right:40px;\">"+
			      "<a id=\"button_send_session_feedback_text\" style=\"border-radius:5px 5px 5px 5px;width:auto;\">Enviar</a>"+
			    "</div>"+
			  "</div>"+
			"</div>";
	public static String DIALOG_SIMPLIFICATION = 
			"<!-- Slider -->"+
			  "<div id=\"slider-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "¿Fue la simplificación de párrafo que pidió útil para comprender mejor el servicio?"+
			    "</div>"+
			    "<form>"+
			      "<input id=\"slider_session_feedback_paragraph\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_paragraph.value\"/>"+
			      "<div class=\"slider-horizontal-text-below-left\">Nada útil</div>"+
			      "<div class=\"slider-horizontal-text-below-right\">Muy útil</div>"+
			      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
			    "</form>"+
			  "</div>"+
			  "<!-- Slider -->"+
			  "<div id=\"slider-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "¿Fue la simplificación de frase que pidió útil para comprender mejor el servicio?"+
			    "</div>"+
			    "<form>"+
			      "<input id=\"slider_session_feedback_phrase\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_phrase.value\"/>"+
			      "<div class=\"slider-horizontal-text-below-left\">Nada útil</div>"+
			      "<div class=\"slider-horizontal-text-below-right\">Muy útil</div>"+
			      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
			    "</form>"+
			  "</div>"+
			  "<!-- Slider -->"+
			  "<div id=\"slider-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "¿Fue la simplificación de palabra que pidió útil para comprender mejor el servicio?"+
			    "</div>"+
			    "<form>"+
			      "<input id=\"slider_session_feedback_word\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_word.value\"/>"+
			      "<div class=\"slider-horizontal-text-below-left\">Nada útil</div>"+
			      "<div class=\"slider-horizontal-text-below-right\">Muy útil</div>"+
			      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
			    "</form>"+
			  "</div>";
	public static String DIALOG_CTZPEDIA = 
			"<!-- Slider -->"+
			"<div id=\"slider-session-feedback-content\">"+
			  "<div class=\"mensaje\">"+
			    "¿Fue útil su consulta a la Citizenpedia?"+
			  "</div>"+
			  "<form>"+
			    "<input id=\"slider_session_feedback_ctz\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_ctz.value\"/>"+
			    "<div class=\"slider-horizontal-text-below-left\">Nada útil</div>"+
			    "<div class=\"slider-horizontal-text-below-right\">Muy útil</div>"+
			    "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
			  "</form>"+
			"</div>";
	public static String DIALOG_TIMEOUT = 
			  "<!-- Comments -->"+
			  "<div id=\"comentarios-session-feedback-content\">"+
			    "<div class=\"mensaje\">"+
			      "La sesión tardó en completarse más de lo acostumbrado. ¿Tuvo algún problema con algún elemento del servicio?"+
			    "</div>"+
			    "<div id=\"session-feedback-comments\" class=\"session-feedback-comments\">"+
			      "<textarea id=\"session-feedback-comments-text\" class=\"session-feedback-comments-text\" placeholder=\"Explique los problemas encontrados...\" cols=\"40\" rows=\"5\" style=\"resize: none;\"></textarea>"+
			    "</div>"+
			  "</div>";
}
