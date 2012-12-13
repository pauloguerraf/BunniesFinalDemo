package ltg.foraging;
import ltg.commons.MessageListener;
import ltg.commons.SimpleXMPPClient;
import ltg.foraging.model.Model;

import org.jivesoftware.smack.packet.Message;

import processing.core.PApplet;

import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.exceptions.JsonParseException;
import com.github.jsonj.tools.JsonParser;


public class XMPPApplet extends PApplet {
	private static final long serialVersionUID = 1L;

	
	// XMMP client
	private SimpleXMPPClient xmpp = null;
	// JSON parser
	private JsonParser parser = new JsonParser();
	// Foraging game
	private Model model = new Model();


	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "XMPPApplet" });
	}



	////////////////////////
	// Processing methods //
	////////////////////////


	public void setup() {
		// Sketch
			// All your sketch stuff here
		
		// Logic
		xmpp = new SimpleXMPPClient("fg-penalty-box@ltg.evl.uic.edu", "fg-penalty-box", "fg-pilot-oct12@conference.ltg.evl.uic.edu");
		println("Connected to chatroom and listening");
		xmpp.registerEventListener(new MessageListener() {
			@Override
			public void processMessage(Message m) {
				processIncomingData(m.getBody());
			}
		});
	}


	public void draw() {
	}



	/////////////////////
	// Drawing methods //
	/////////////////////

	


	////////////////////////////
	// Event handling methods //
	////////////////////////////

	public void processIncomingData(String s) {
		JsonObject json = null;
		JsonElement jsone = null;
		try {
			jsone = parser.parse(s);
			if (!jsone.isObject()) {
				// The element is not an object... bad...
				return;
			}
			json = jsone.asObject();
			// Pick the right JSON handler based on the event type
			if (isRFIDUpdate(json)) {
				updateLocation(json);
//			} else if (otherEvent) {
//				processOtherEvent();
			}
		} catch (JsonParseException e) {
			// Not JSON... skip
			//System.err.println("Not JSON: " + s);
		}
	}
	
	
	private boolean isRFIDUpdate(JsonObject json) {
		if (json.getString("event")!= null && 
				json.getString("event").equals("rfid_update") && 
				json.getString("destination")!= null && 
				json.getObject("payload") != null)
			return true;
		return false;
	}
	
	
	
	private void updateLocation(JsonObject json) {
		String dest = json.getString("destination");
		for (JsonElement a : json.getArray("payload", "arrivals")) {
			// Process arrival
			model.arrival(dest, a.asPrimitive().asString());
		}
		for (JsonElement a : json.getArray("payload", "departures")) {
			// Process departure
			model.departure(dest, a.asPrimitive().asString());
		}

	}


}
