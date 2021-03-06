package eu.musesproject.client.db.entity;

public class ContextEvent {
	
	private int id;
	private int actionId;
	private String type;
	private long timestamp;

	public ContextEvent() {
	}
	public ContextEvent(int id, int actionId, String type, long timestamp) {
		this.id = id;
		this.actionId = actionId;
		this.type = type;
		this.timestamp = timestamp;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getActionId() {
		return actionId;
	}
	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "ContextEvent [id=" + id + ", actionId=" + actionId + ", type="
				+ type + ", timestamp=" + timestamp + "]";
	}
	
}