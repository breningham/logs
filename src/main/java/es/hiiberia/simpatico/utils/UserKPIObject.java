package es.hiiberia.simpatico.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import es.hiiberia.simpatico.rest.SimpaticoResourceIFE;

public class UserKPIObject {

	private String user;
	private Date initial;
	private Date end;
	private boolean formComplete;
	private List<String> data;
	
	public UserKPIObject(String user, Date date) {
		this.user = user;
		this.initial = date;
		this.end = date;
		this.formComplete = false;
		this.data = new ArrayList<>();
	}
	
	public String getUser() {
		return this.user;
	}
	
	public Date getInitialDate() {
		return this.initial;
	}
	
	public Date getEndDate() {
		return this.end;
	}
	
	public long getDiffDateMS() {
		return this.end.getTime() - this.initial.getTime();
	}
	
	public boolean isFormComplete() {
		return this.formComplete;
	}
	
	public void addDate(Date date, String event) {
		if (date.after(this.end)) {
			this.end = date;
		} else if (date.before(this.initial)) {
			this.initial = date;
		} else {
			Logger.getRootLogger().debug("[USER-KPI-OBJECT] Date not used. this.initial: " + this.initial.toString() + ", this.end: " + this.end.toString() + ", date to add: " + date.toString());
		}
		
		if (event.equalsIgnoreCase(SimpaticoResourceIFE.EVENT_FORM_END)) {
			this.formComplete = true;
		}
		
		data.add("date: " + date + ", date milis: "+ date.getTime());
	}
}
