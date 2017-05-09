package es.hiiberia.simpatico.utils;

/**
 * 
 * This class get the language from the website and constructs the SF dialog to show to the user.
 *
 */
public class Forms {
	// i18n
	private String button_cancel;
	private String button_send;
	private String slider_useful;
	private String slider_unuseful;
	
	private String common_faces;
	private String common_comments;
	private String common_opinion_placeholder;
	
	private String simpl_paragraph;
	private String simpl_phrase;
	private String simpl_word;
	
	private String ctz_slider;
	
	private String timeout;
	private String timeout_placeholder;
	
	public Forms(String lang) {
		if (lang.equals("es")) {
			/** Spanish **/
			button_cancel = "No quiero";
			button_send = "Enviar";
			slider_useful = "Muy útil";
			slider_unuseful = "Nada útil";
			
			common_faces = "¿Cree que en conjunto las herramientas de SIMPATICO le facilitaron la comprensión del servicio?";
			common_comments = "¿Tiene alguna sugerencia para los desarrolladores de SIMPATICO?";
			common_opinion_placeholder = "Escriba su opinión...";
			
			simpl_paragraph = "¿Fue la simplificación de párrafo que pidió útil para comprender mejor el servicio?";
			simpl_phrase = "¿Fue la simplificación de frase que pidió útil para comprender mejor el servicio?";
			simpl_word = "¿Fue la simplificación de palabra que pidió útil para comprender mejor el servicio?";
			
			ctz_slider = "¿Fue útil su consulta a la Citizenpedia?";
			
			timeout = "La sesión tardó en completarse más de lo acostumbrado. ¿Tuvo algún problema con algún elemento del servicio?";
			timeout_placeholder = "Explique los problemas encontrados...";
		} else if (lang.equals("it")) {
			/** Italian **/
			button_cancel = "Annulla";
			button_send = "Inviare";
			slider_useful = "Molto utile";
			slider_unuseful = "Non è utile";
			
			common_faces = "Pensi che insieme strumenti di SIMPATICO ti ha facilitato la comprensione del servizio?";
			common_comments = "Hai qualche suggerimento per gli sviluppatori di SIMPATICO?";
			common_opinion_placeholder = "Scrivi una recensione...";
			
			simpl_paragraph = "E 'stato semplificando punto che ha chiamato utile per capire meglio il servizio?";
			simpl_phrase = "È stata la semplificazione della frase che ha chiamato utile per capire meglio il servizio?";
			simpl_word = "E 'stato semplificando parola chiamata utile per capire meglio il servizio?";
			
			ctz_slider = "Quanto utile è stata la sua richiesta al Citizenpedia?";
			
			timeout = "La sessione ha preso per completare più del solito. Hai avuto problemi con qualsiasi elemento del servizio?";
			timeout_placeholder = "Spiegare i problemi incontrati...";
		} else {
			/** English (default) **/
			button_cancel = "Cancel";
			button_send = "Send";
			slider_useful = "Very useful";
			slider_unuseful = "Not useful";
			
			common_faces = "Do you think that the SIMPATICO tools together facilitated the understanding of the service?";
			common_comments = "Do you have any suggestions for SIMPATICO developers?";
			common_opinion_placeholder = "Write your review...";
			
			simpl_paragraph = "Was the paragraph simplification you asked for useful to better understand the service?";
			simpl_phrase = "Was the simplification of sentence you asked for useful to better understand the service?";
			simpl_word = "Was the simplification of word you asked for useful to better understand the service?";
			
			ctz_slider = "How useful was your inquiry to the Citizenpedia?";
			
			timeout = "The session took longer than usual. Did you have a problem with any element of the service?";
			timeout_placeholder = "Explain the problems encountered...";
		}
	}
	

	// Pieces of different form's divs
	public String getStartingDiv() {
		return "<div id=\"dialog_modal_session_feedback_a\" class=\"modalligazon modal-session-feedback\">";
	}
	
	public String getCommonPart() {
		return "<!-- Faces -->"+
				  "<div id=\"faces-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				      common_faces +
				    "</div>"+
				    "<div id=\"face-radio-buttons-session-feedback\" class=\"cc-selector\">"+
				      "<input id=\"face-happy-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"happy\"/>"+
				      "<label for=\"face-happy-session-feedback\" data-face=\"happy\" class=\"left\" style=\"margin-left: 10%;border-radius: 20%;\"><img src=\"img/happy_face.png\" alt=\"Happy\"/></label>"+
				      "<input id=\"face-normal-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"normal\"/>"+
				      "<label for=\"face-normal-session-feedback\" data-face=\"normal\" style=\"margin-left: 20%;border-radius: 20%;\"><img src=\"img/normal_face.png\" alt=\"Normal\"/></label>"+
				      "<input id=\"face-sad-session-feedback\" class=\"input_hidden\" name=\"faces-session-feedback\" type=\"radio\" value=\"sad\"/>"+
				      "<label for=\"face-sad-session-feedback\" data-face=\"sad\" class=\"right\" style=\"margin-right: 10%;border-radius: 20%;\"><img src=\"img/sad_face.png\" alt=\"Sad\" /></label>"+
				    "</div>"+
				  "</div>"+
				  "<!-- Comments -->"+
				  "<div id=\"comentarios-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				      common_comments +
				    "</div>"+
				    "<div id=\"session-feedback-comments\" class=\"session-feedback-comments\">"+
				      "<textarea id=\"session-feedback-comments-text\" class=\"session-feedback-comments-text\" placeholder=\""+ common_opinion_placeholder +"\" cols=\"40\" rows=\"5\" style=\"resize: none;\"></textarea>"+
				    "</div>"+
				  "</div>"+
				  "<!-- Buttons send/cancel -->"+
				  "<div id=\"buttons_session_feedback\" style=\"padding-top: 50px;padding-bottom: 20px\">"+
				    "<div class=\"mais button_cancel_session_feedback\" style=\"position:relative;float:left;margin-left:40px;\">"+
				      "<a id=\"button_cancel_session_feedback_text\" style=\"border-radius:5px 5px 5px 5px;width:auto;\">"+ button_cancel +"</a>"+
				    "</div>"+
				    "<div class=\"mais\" id=\"button_send_session_feedback_a\" style=\"position:relative;float:right;margin-right:40px;\">"+
				      "<a id=\"button_send_session_feedback_text\" style=\"border-radius:5px 5px 5px 5px;width:auto;\">"+ button_send +"</a>"+
				    "</div>"+
				  "</div>"+
				"</div>";
	}
			
	public String getSimplificationPart() {
		return "<!-- Slider -->"+
				  "<div id=\"slider-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				    	simpl_paragraph +
				    "</div>"+
				    "<form>"+
				      "<input id=\"slider_session_feedback_paragraph\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_paragraph.value\"/>"+
				      "<div class=\"slider-horizontal-text-below-left\">"+ slider_unuseful +"</div>"+
				      "<div class=\"slider-horizontal-text-below-right\">"+ slider_useful +"</div>"+
				      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
				    "</form>"+
				  "</div>"+
				  "<!-- Slider -->"+
				  "<div id=\"slider-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				     	simpl_phrase +
				    "</div>"+
				    "<form>"+
				      "<input id=\"slider_session_feedback_phrase\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_phrase.value\"/>"+
				      "<div class=\"slider-horizontal-text-below-left\">"+ slider_unuseful +"</div>"+
				      "<div class=\"slider-horizontal-text-below-right\">"+ slider_useful +"</div>"+
				      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
				    "</form>"+
				  "</div>"+
				  "<!-- Slider -->"+
				  "<div id=\"slider-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				      simpl_word +
				    "</div>"+
				    "<form>"+
				      "<input id=\"slider_session_feedback_word\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_word.value\"/>"+
				      "<div class=\"slider-horizontal-text-below-left\">"+ slider_unuseful +"</div>"+
				      "<div class=\"slider-horizontal-text-below-right\">"+ slider_useful +"</div>"+
				      "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
				    "</form>"+
				  "</div>";
	}
			
	public String getCtzPart() {
		return  "<!-- Slider -->"+
				"<div id=\"slider-session-feedback-content\">"+
				  "<div class=\"mensaje\">"+
				  	ctz_slider +
				  "</div>"+
				  "<form>"+
				    "<input id=\"slider_session_feedback_ctz\" class=\"slider\" type=\"range\" min=\"-5\" max=\"5\" step=\"1\" value=\"0\" oninput=\"sliderOutputSessionFeedback.value = slider_session_feedback_ctz.value\"/>"+
				    "<div class=\"slider-horizontal-text-below-left\">"+ slider_unuseful +"</div>"+
				    "<div class=\"slider-horizontal-text-below-right\">"+ slider_useful +"</div>"+
				    "<div id=\"slider-output-session-feedback\" style=\"text-align: center;\"><output id=\"sliderOutputSessionFeedback\">0</output></div>"+
				  "</form>"+
				"</div>";
	}
			
	public String getTimeoutPart() {
		return "<!-- Comments -->"+
				  "<div id=\"comentarios-session-feedback-content\">"+
				    "<div class=\"mensaje\">"+
				    	timeout +
				    "</div>"+
				    "<div id=\"session-feedback-comments\" class=\"session-feedback-comments\">"+
				      "<textarea id=\"session-feedback-timeout-text\" class=\"session-feedback-comments-text\" placeholder=\""+ timeout_placeholder +"\" cols=\"40\" rows=\"5\" style=\"resize: none;\"></textarea>"+
				    "</div>"+
				  "</div>";
	}	  
}
