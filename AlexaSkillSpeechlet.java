package com.amazon.customskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ThreadLocalRandom;
import com.amazon.speech.speechlet.SpeechletV2;


public class AlexaSkillSpeechlet
implements SpeechletV2
{
	
	//Nötiges
			static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
			public static enum raum {bedchamber, entrencehall, garden, kitchen, library, lounge, servantbedroom, servantwing};
			public static String aktuellerraum = " ";
			public static enum zeugen {butler, cat, child, cook, gardener, lady, lord, maid};
			public static String aktuellerzeuge= " ";
			public static String text="<speak>"; //dies ist die Variable, die von speak gefüllt und dann durch send() gesendet wird
			//Zustand
			public static int zeugenzeit=1;
			public static int raumzeit=1;
			public static int tutorialzeit=1;
			public static int solvezeit=1;
			public static int watsonzeit=1;
			private static boolean inbefragung=false;
			private static boolean inraum=false;
			private static boolean tutorial=true;
			private static boolean insolve=false;
			private static boolean inwatson=false;
			//userintents
			private static enum userintent {yes, no, talk, examplequestion, where, suspicion, others, weird, relation, stop, watson, search, leave, solve};
			public static userintent ouruserintent;
			public static String userrequest;	
			//Sätze	
			public static String frageaufruf="what do you want to ask this witness? If you some need example questions, ask me.";
			public static String exampleq = "The main things you need to find out to solve the mystery successfully are where the people have been when the crime has happened, if they have any suspicion who could have done such terrible things, if something did seem weird to them lately. Furthermore, you should ask the persons about their relationships with the deceased Lord and maybe if they knew some things about the other persons living there and if they could have done the crime. Do you want to listen to a few example questions and if so, which?";//Die Examplequestions müssen rein
			//Einleitung
			public static String einleitung1 = "It was a dark and stormy night, when you and your assistant Watson were invited to Furbish Manor. The rain hammered on the roof of the cab you were using to get there. You stand in front of the dark iron gate and wait that somebody comes to let you in. The wind is cold and you have to wait a moment in this unpleasant place. Watson looks at you" ;
			public static String tutorialfrage="Do you remember how we normaly solve a case? Yes or No?";
			public static String einleitung2 = "After a few minutes finally somebody opens the gate to let you in. Together you walk the street up the hill until you come to the entry. The door opens and you enter.";
			//Tutorial
			public static String tutorial1 = "Its really not that complicated: We walk around a house and ask the people we meet the questions we want to ask them. They answer. Most often they have seen something or have not seen something in a while. You should ask them for their alibis and who can prove them. Maybe somebody is lying, so you should listen carefully. Did you understand that? Yes or no?";
			public static String tutorial2 = "If we enter the room you will get a general description of what is in it. You can look at some things, if you want to learn more about them. Should i repeat what i just said? Yes or no?";
			public static String tutorial3 = "If you forget anything that you have found you can always ask me. Just say “Watson” and i will be there. I can give you a short summary and some more tipps. Did you understand me?";


			//Alles über Räume
			//entryhall
			static boolean entrencehallbesucht = false;
			static String entrencehall []=
				/*beschreibung [0]*/	{"The Entrance Hall is dimly lit and feels calm and empty. The eerie silence is only disturbed by the echo of your steps on the cold marble floor and the big doors closing behind you. You see a grande staircase leading up to several doors, a door to the lounge, the kitchen, the bedchamber, the library, the garden and the servants wing. Next to the staircase are two knights armor on each side. A butler is standing next to one of the doors. What do you want to do?",
						/*wwyd [1]*/			"What do you want to do in the entrance hall? Would you like to speak to the witness, search the room or leave the room?",
						/*detail [2]*/			"You take a closer look around. Nothing seems unusual to you on the first glance. Everything is perfectly cleaned and there are no traces of an intruder. The murderer must be someone within the house. Then you see it. One of the knights armors positioned next to the staircase is missing it's sword, which could be a murder weapon.",
						/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the library?",
			/*zeuge [4]*/			"butler"};
			//library	
			static boolean librarybesucht = false;
			public static String library []=
				/*beschreibung [0]*/	{"WOW! That's a library like it is said in the books. Like bells library from beauty and the beast. High walls and shelves filled with books everywhere around you.  There are even these movable ladders so you can get to the higher shelves that you know from those movies. Also the smell is as you always imagined it. There are comfortable armchairs that invite you to linger and in the background ist sof music playing. There's no one to be seen here far and wide... But there's something moving back there, isn't it? Ahh, a cat! It looks sweet and fluffy, do you want to pet it?",
						/*wwyd [1]*/			"What do you want to do in the library? Would you like to search the room, pet the cat or leave the room?",
						/*detail [2]*/			"You see that the cat was treated better than the servants. It has a red bow of silk tied around it's neck and the porcelain bowls in the corner are full of fresh meat and water. Someone must tend to it heartwarmingly.",
						/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the entrencehall?",
			/*zeuge [4]*/			"cat"};
			//garden	
			static boolean gardenbesucht = false;
			public static String garden []=
				/*beschreibung [0]*/	{"You enter the garden and can hardly believe your eyes. A large weeping willow surrounded by the most beautiful flowerbeds you have ever seen is in the center of the large field. An old broken bench looks grim underneath the willow. A small pond next to the tree reflects the silver moonlight. Fruit trees frame a narrow path, which you follow down to a greenhouse. Bent over a vegetable patch, you see a gardener, an old man with soft friendly features. When he notices you, he turns around and smiles encouragingly at you. What do you want to do?",
						/*wwyd [1]*/			"What do you want to do in the Garden? Would you like to speak to the witness, search the garden or leave the garden?",
						/*detail [2]*/			"You see that the Gardner's forehead is covered in sweat, but you aren't sure whether it's from the hard work or he is hiding something. ",
						/*whroom [3]*/			"Where do you want to go: The entrencehall? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the library?",
			/*zeuge [4]*/			"gardener"};
			//servantbedroom	
			static boolean servantbedroombesucht = false;
			public static String servantbedroom []=
				/*beschreibung [0]*/	{"You've come through the servants' wing and you see an open door, you look in. Here, compared to the hallway, it looks really nice and cosy, despite the uncomfortable walls surrounding the room. There is a small bed, a chest of drawers with a mirror and a narrow closet in the room. A girl is sitting on the stool in front of the chest of drawers, you think that it can only be the maid. She holds a photo in her hand. When she notices you, she quickly lets it disappear, but you catch a last glimpse of it. It's a photo of the deceased lord with the maid...What was the connection between these two? She looks frightened and at the same time infinitely sad. ",
						/*wwyd [1]*/			"What do you want to do in the servants bedroom? Would you like to speak to the maid, search the room or leave the room?",
						/*detail [2]*/			"The girl is clearly madly in love with the Lord. Was the relationship one sided or was he involved too? She looks fairly innocent, you don't think that she could've commited such a gruesome murder. Maybe an act of jealousy?",
						/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servants wing? The entrencehall? The bedchamber or the library?",
			/*zeuge [4]*/			"maid"};
			//kitchen	
			static boolean kitchenbesucht = false;
			public static String kitchen []= 
				/*beschreibung [0]*/	{"You're standing in the heart of the house now, here's the kitchen. It smells and bubbles everywhere. It scents like freshly cooked food. On the wall are pots and pans hanging and you hear dishes clattering. You look to the right and see a large table full of delicacies: Sliced fruit, vegetables, various sauces and bowls with meat and potatoes and much more. Next to them are a lot of fresh herbs. The cook also stands here and tries to place more food on the table. What do you want to do",
						/*wwyd [1]*/			"What do you want to do in the kitchen? Would you like to speak to the witness, search the room or leave the room?",
						/*detail [2]*/			"You look around and notice that the table is so full of food that there isn't a single empty square inch of free space. This amount would take hours, maybe even days to cook. You also notice that several knifes are missing from the knife block.",
						/*whroom [3]*/			"Where do you want to go: The garden? The entrencehall? The lounge? The servants wing? The Servants bedroom? The bedchamber or the library?",
			/*zeuge [4]*/			"cook"};
			//servantwing	
			public static boolean servantwingbesucht = false;
			public final static String servantwing []= 
				/*beschreibung [0]*/	{"You get into a dark corridor with bare walls, a single flickering light bulb hangs from the ceiling. The wooden planks under your feet creak and the air is cold and somehow damp. This is by far the creepiest room in the castle. But you hear another sound, what is that? It's the kitchen boy playing on the floor. He has some wooden toys that have the best behind them. But he looks quite happy the way he sits there. When he sees you he is frightened, but the creaking planks should have announced you? Strange. He crouches down a bit, you calm him down and ask why he is afraid...",
						/*wwyd [1]*/				"What do you want to do in the servants wing? Would you like to speak to the witness, search the room or leave the room?",
						/*detail [2]*/			"You don't notice anything exceptional in corridor. It's cold and damp like every other servant corridor. People don't care whether servants are comfortable or not.",
						/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrencehall? The Servantbedroom? The bedchamber or the library?",
			/*zeuge [4]*/			"child"};
			//bedchamber	
			public static boolean bedchamberbesucht = false;
			public static String bedchamber []= 
				/*beschreibung [0]*/	{"You open the door into a luxurious Bedroom. Floorlength Windows almost hidden beneath heavy velvet curtains shine silver light into the spacious room which is full of handcarved furnishings. Detailed paintings in golden Frames decorate every inch of the walls and a big carpet covers the finely polished parquet. An elderly Lady strutts absently through the room.One expensive garment after another disappears into the already empty closet of the recently deceased Lord. When she notices you, she looks frightened for a split second though she catches herself quickly and starts sobbing without shedding a tear. She grabs your arm and looks out of the window, all in one dramatic gesture.",
						/*wwyd [1]*/			"What do you want to do in the Bed chamber? Ask the withness,search the room or leave the room?",
						/*detail [2]*/			"You see that the Lord's mother, the lady, isnt as destressed with the death of her son as she is trying to convey. She has already cleaned out every single piece of her sons clothing from his bedroom.",
						/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrencehall? The Servantbedroom? The servantwing or the library?",
			/*zeuge [4]*/			"lady"};
			//lounge	
			public static boolean loungebesucht = false;
			public static String lounge []= 
						/*beschreibung [0]*/	{"As you enter the scene of the crime thunder disturbs the gloomy night. You see a fireplace which fire has gone out a long time ago. Two leather armchairs and a small table opposite of the fireplace seem to have been pushed aside. Expensive liquor that once was on the table now was spilled all over the carpet. The body of the deceased is laying behind the armchair, as if he tried to create distance between him and his murderer. His eyes are still wide-open in fear. His elegant clothing is now drenched in his own blood coming out of a single stab wound in the middle of his chest. ",
						/*wwyd [1]*/			"What do you want to do in the Lounge? Would you like to search the room or leave the room?",
						/*detail [2]*/			"When you take a closer look you see that one corner of the carpet is folded under itself. Someone could easily trip over it. ",
						/*whroom [3]*/			"Where do you want to go: The kitchen? The garden? The entrencehall? The servants wing? The Servants bedroom? The bedchamber or the library?",
			/*zeuge [4]*/			"lord"};

			//Alles über Zeugen
			//butler
			public static boolean butlerbefragt=false;
			public static String jamesbeschr = "The Butler of the house is a grim looking lanky man with pronounced eyebrows and a hooked nose reaching almost down to his lips. He is wearing a worn down, but meticulously cleaned suit.";
			public static String butleraussagen[]=
					//where[0],relation[1],weird[2],susp[3],others[4]
				/*where[0]*/	{"Polishing silver cutlery with the Maid. Why are you asking? Do I seem like a murderer. I have been working here since a long, long time. I’m becoming older, don’t want to risk or lose my job, understand detective?",
						/*relation[1]*/ "Young ‘n stupid man, you know. Had a lot to learn, lately. We Didn’t really have a connection. I was just doing my job here, man.",
						/*weird[2]*/    "A lot of things seem weird these days, don’t  you think? The Old Lady has acted strangely… Bought a lot of stuff. Go ask her and leave me alone.",
						/*susp[3]*/     "Detective, I’m not gonna tell you the secrets of this house. Never have, never will.",
			/*others[4]*/   "I was with the maid. Don’t know anything about the others, mind my own business, you know."};
			//Zusammenfassungen von  butler
			public static String[] butlerzsmf= {"","","","",""};

			//child	
			public static boolean childbefragt=false;
			public static String childbeschr = "A little boy, at the age of 6. He has brown hair and on his shirt there is a red blot, possibly cherry sauce. He is the son of the cook";
			static String child1 = "";
			static String child2 = "";
			static String child3 = "";
			//zusammenfassung von child
			public static String[] child= {"","","","",""};

			//cook	
			public static boolean cookbefragt=false;
			public static String cookbeschr = "The cook is probably the friendliest and most loving person in the whole house. She cares for everyone and makes sure everyone gets enough to eat. She also stands for the mistakes of others. She had a very good relationship with the Lord, although the mother hates this. What do you want to ask";
			public static String cookaussagen[]=
					//where[0],relation[1],weird[2],susp[3],others[4]
					/*where[0]*/	{"Right here Sir. The Lady wanted to have an extravagant supper. I was cooking, baking and preparing all day for it. I’m still not ready Sir.",
					/*relation[1]*/ "Didn’t really have any. I’m in the kitchen or in the servants wing most of the time. I don’t have enough time for chitchat most of the time. And they treat us as what we are, servants.",
					/*weird[2]*/    "Not that i noticed of. I am focussed on my craft most of the time though.",
					/*susp[3]*/     "Not that i can think of. The Lord payed and treated us well. We are lucky to be here Sir.",
					/*others[4]*/   "I know that my boy was playing in the servant wings. I’ve heard him pretending to be the prince."};
			// Zusammenfassungen von cook
			public static String[] cookzsmf= {"","","","",""};

			//gardener	
			public static boolean gardenerbefragt=false;
			public static String gardenerbeschr = "The gardener is a very simple-minded man. He seems to have been doing his job well for many years. He is said to have been in love with the lady many years ago. Well, in any case he loves his daughter, the maid, more than anything and would probably do anything for her. What do you want to ask?";
			public static String gardeneraussagen[]=
					//where[0],relation[1],weird[2],susp[3],others[4]
					/*where[0]*/	{"I was talking to my daughter, she is the wonderful maid that’s working for the Lord himself. Marvelous girl, if you ask me. Only the best in her mind for everyone. I think she deserved better than being a  maid. I told her to go to the East of the Country and get a better life. But she did not agree with me. I’m sorry, that hasn’t anything to do with your question. ",
					/*relation[1]*/ "I’m only the Gardener, working so that my daughter could have a better life. I’m a gentleman in contrast to the lord, but that’s on a different page. We had a decent relationship. Not to close, but decent.",
					/*weird[2]*/    "Not at all! It’s like it has ever been around here. But if you want to ask a strange man around here, go to the Butler, James. Ask him the questions...It’s such a tragedy what happened. I hope you find the right man, detective.",
					/*susp[3]*/     "James didn’t really like him, but he hasn’t had enemies, so far. I guess everyone liked him but no one knew him for real.",
				/*others[4]*/   "I know my daughter was with me, can’t tell you more. I am sorry for that detective."};
			//zusammenfassung von gardener
			public static String[] gardenerzsmf= {"","","","",""};

			//lady	
			public static boolean ladybefragt=false;
			public static String ladybeschr = "The old lady is the mother of the late lord. She is an old woman but very well groomed with lots of make-up and a striking hairstyle. She wears a lot of jewellery and excessive embelishments.";
			public static String ladyaussagen[]=
					//where[0],relation[1],weird[2],susp[3],others[4]
				/*where[0]*/	{"I was in the garden, having a walk and relaxing beneath the willow. I had wanted to try out my new walking dress.",
						/*relation[1]*/ "He was my only child. My baby. His father, my deceased husband, poisoned him with his thoughts of equality. Wanted to be one of the commoners. Said that nobody should have servants. I hoped that he would come to reason. He should’ve listened to me! Commoners will bite of your arm if you offer them a hand. Now he is dead... Well at least I can use his life insurance money to remarry and bury my sorrow in diamonds.",
						/*weird[2]*/    "The shipment of the dresses i bought last week haven’t arrived yet. I need to write them a stern letter if they don’t arrive soon.",
						/*susp[3]*/     "Surely it was one of the dirty servants. He was way too nice to them, paid them way too much too. They probably wanted to extort them for even more money.",
			/*others[4]*/   "I know that the gardner wasn’t in the garden. He get’s me some fruit of the trees usually, but I didn’t see him today."};
			//Zusammenfassungen der lady
			public static String[] ladyzsmf= {"","","","",""};

			//maid	
			public static boolean maidbefragt=false;
			public static String maidbeschr = "The maid is a shy, young and innocent girl who is satisfied with little. She is not conspicuous but if you look at her for a long time you will notice that she is beautiful. As a prince, you could easily fall for that, don't you think?";
			public static String maidaussagen[]=
					//where[0],relation[1],weird[2],susp[3],others[4]
					/*where[0]*/	{"I was polishing the silver cutlery together with james all day long. He can tell that we did that too! Don’t you dare think I was the murderer of my beloved Lord. ",
					/*relation[1]*/ "I...We… We were good friends. We grew up together, i guess. He is one of the most important persons I have...I had.",
					/*weird[2]*/    "I don't know.. I don't know.. I’m sorry I have to go now…",
					/*susp[3]*/     "I can not think of one person in this whole house who would do this, ever! I mean, James did not really like him but he was polishing the silver cutlery with me, so he’s out. He wasn’t best friends with my father but he would have never done it! NEVER!",
			/*others[4]*/	"I only know me and James were polishing… The cook was cooking all day, for sure. Otherwise I don’t know what everyone does the whole day, why should I?"};
			//zusammenfassung von maid
			public static String[] maidzsmf= {"","","","",""};	
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins, onSessionstarted");
		
	}
	
	
	//yesno():
	public static boolean yesno(String UserRequest) {
		logger.info("yesno wurde gestartet");
		logger.info("User: "+UserRequest);
		String yes = "yes*?";
		String no = "no*?";
		Pattern p1 = Pattern.compile(yes);
		Matcher m1 = p1.matcher(UserRequest);
		Pattern p2 = Pattern.compile(no);
		Matcher m2 = p2.matcher(UserRequest);
		logger.info("Matcher wurden vorbereitet");
		if (m1.find()) {
			logger.info("yes wurde erkannt");
			//ouruserintent=userintent.yes;
			//logger.info("ouruserintent wurde zu"+ouruserintent+"geändert");
			return true;} 
		else if (m2.find()) {
			logger.info("no wurde erkannt");
			//ouruserintent=userintent.no;
			//logger.info("ouruserintent wurde zu"+ouruserintent+"geändert");
			return false;} 
		logger.info("Es gab einen fehler bei Yesno");
		pardon("watson");
		return true;}	

	//send()
	public static SpeechletResponse sende() {
		//logger.info("send() wurde gestartet");
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		text=text+"</speak>";
		logger.info("Der gesendete Text lautet:"+text);
		speech.setSsml(text);
		text="<speak>";
		//logger.info("text wurde zurückgesetzt auf:"+text);
		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");
		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);
		return SpeechletResponse.newAskResponse(speech, rep);
		
	};
	//speak	verändert den String text, welcher von sende() zu einer ssml verarbeitet und versendet wird. So können cerschiedene Leute hintereinander sprechen
	public static void speak(String sentence, String name) {
		//logger.info("speak wurde gestartet");
		logger.info(name+": "+sentence);
		switch(name){ 
		case "narrator": 
			//logger.info("Wir sind jetzt im Fall Narrator" + text);
			text=text+"<voice name=\"Matthew\"> "+ sentence + "</voice>";
			break;
		case "watson": 
			//logger.info("Wir sind jetzt im Fall Watson" + text);
			text=text+"<voice name=\"Brian\"> "+ sentence + "</voice>";
			break; 
		case "maid": 
			text=text+"<speak><voice name= \"Kimberly\"><lang xml:\"en-US\"> "+ sentence + "</lang></voice></speak>";
			break; 
		case "gardener": 
			text=text+"<speak><voice name= \"Russel\"><lang xml:\"en-AU\">" + sentence + "/lang></voice></speak>";
			break; 
		case "butler": 
			text=text+"<speak><voice name= \"Joey\"><lang xml:\"en-US\">" + sentence + "</lang></voice></speak>";
			break;
		case "cook": 
			text=text+"<speak><voice name= \"Kendra\"><lang xml:\"en-US\">" + sentence + "</lang></voice></speak>";
			break;
		case "child": 
			text=text+"<speak><voice name= \"Justin\"><lang xml:\"en-US\">" + sentence + "</lang></voice></speak>";
			break;
		case "lady": 
			text=text+"<speak><voice name= \"Amy\"><lang xml:\"en-GB\">" + sentence + "</lang></voice></speak>";
			break;
		default:
			text=text+"<speak><voice name= \"Matthew\"><lang xml:\"en-US\"> "+ sentence + "</lang></voice></speak>";
		logger.info("text enthält jetzt: "+text);
		}
		return;
	}

	//pardon() ist eine funktion, falls etwas nicht verstanden wurde
	public static void pardon(String name) {
		logger.info("funktion nicht verstanden");
		speak("sorry, could you repeat that?", name);
		return;}	

	//wasfragst - Funktion um herauszufinden, was der User fragt
	public static int wasfragst(String userRequest) {
			logger.info("funktion reggex");
			userRequest = userRequest.toLowerCase();

			// room
			String leave ="(i want to |i would like to )?(leave|exit)";
			String search ="(i want to |i would like to )?(search|look)";
			String whitness ="(i want to |i would like to )?(ask|talk|question)( to a |to the )?(whitness|victim|butler|maid|gardener|lady|lord|child|cook)?";
			String where = "where( have you been )?(when the |during the )?(crime was committed|murder was committed|the murder happened|the crime happened)?";
			String relationship ="(what was your |did you )?(relationship|like|hate|love)( with)?( the lord| the victim)?";
			String weird ="(did something seem )?weird( to you )?(lately|recently)?";
			String suspicion="(do you have any )?suspicion( who killed | who commited )?(the lord|the crime)?";
			String others="(could you imagine)? others committing (such a)? crime";
			String anythingelse ="(i would like to )?(ask|hear|question)( anything| something)?( else)?";
			String solve= "(i would like to |i want to )?(solve|answer|solution)( the mystery| the game| the murder)?";
			String solvebutler ="(it was the |the murderer is the )?butler";
			String solvechild ="(it was the |the murderer is the )?child";
			String solvemaid ="(it was the |the murderer is the )?maid";
			String solvegardener="(it was the |the murderer is the )?gardener";
			String solvecook="(it was the |the murderer is the )?cook";
			String solvelady="(it was the |the murderer is the )?lady";
			String breakupsolving="(i want to )?(break up solving|stop solving)";

			//watson
			String watson = "(i would like to |i want to )?(ask )?watson( please)?";
			String askzsmfassung= "(i would like to hear |i would like to listen )?(a |the )?summary( please)?";
			String askvokabeln ="";
			String examplequestions="(i would like to hear | i would like to listen )?(the |to )?example questions";

			//manual
			String manual ="(please )?(explain|manual|tutorial)";
			String repeatmanual ="(can you |would you )?repeat( the manual| the tutorial| the explaination)";

			Pattern p1 = Pattern.compile(leave);
			Matcher m1 = p1.matcher(userRequest);
			Pattern p2 = Pattern.compile(search);
			Matcher m2 = p2.matcher(userRequest);
			Pattern p3 = Pattern.compile(whitness);
			Matcher m3 = p3.matcher(userRequest);
			Pattern p4 = Pattern.compile(where);
			Matcher m4 = p4.matcher(userRequest);
			Pattern p5 = Pattern.compile(relationship);
			Matcher m5 = p5.matcher(userRequest);
			Pattern p6 = Pattern.compile(weird);
			Matcher m6 = p6.matcher(userRequest);
			Pattern p7 = Pattern.compile(suspicion);
			Matcher m7 = p7.matcher(userRequest);
			Pattern p8 = Pattern.compile(others);
			Matcher m8 = p8.matcher(userRequest);
			Pattern p9 = Pattern.compile(anythingelse);
			Matcher m9 = p9.matcher(userRequest);
			Pattern p14 = Pattern.compile(solve);
			Matcher m14 = p14.matcher(userRequest);
			Pattern p15 = Pattern.compile(solvebutler);
			Matcher m15 = p15.matcher(userRequest);
			Pattern p16 = Pattern.compile(solvemaid);
			Matcher m16 = p16.matcher(userRequest);
			Pattern p17 = Pattern.compile(solvechild);
			Matcher m17 = p17.matcher(userRequest);
			Pattern p18 = Pattern.compile(solvegardener);
			Matcher m18 = p18.matcher(userRequest);
			Pattern p19 = Pattern.compile(solvecook);
			Matcher m19 = p19.matcher(userRequest);
			Pattern p20 = Pattern.compile(solvelady);
			Matcher m20 = p20.matcher(userRequest);
			Pattern p21 = Pattern.compile(breakupsolving);
			Matcher m21 = p21.matcher(userRequest);
			Pattern p22 = Pattern.compile(watson);
			Matcher m22 = p22.matcher(userRequest);
			Pattern p23 = Pattern.compile(askzsmfassung);
			Matcher m23 = p23.matcher(userRequest);
			Pattern p24 = Pattern.compile(examplequestions);
			Matcher m24 = p24.matcher(userRequest);
			Pattern p25 = Pattern.compile(manual);
			Matcher m25 = p25.matcher(userRequest);
			Pattern p26 = Pattern.compile(repeatmanual);
			Matcher m26 = p26.matcher(userRequest);
					if (m1.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach Beispielfragen gefragt");
						//ouruserintent=userintent.examplequestion;
						return 1;} 
					else if (m2.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach dem Aufenthaltsortgefragt");
						//ouruserintent=userintent.where;
						return 2;} 
					else if (m3.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach Beziehungen gefragt");
						//ouruserintent=userintent.relation;
						return 3;} 
					else if (m4.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach Merkwürdigem gefragt");
						//ouruserintent=userintent.weird;
						return 4;} 
					else if (m5.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach Verdächtigungen gefragt");
						//ouruserintent=userintent.suspicion;
						return 5;}
					else if (m6.find()) {
						logger.info("Bei der Zeugenbefragung wurde nach den Anderen gefragt");
						//ouruserintent=userintent.others;
						return 6;}
					else if (m7.find()) {
						logger.info("Die Zeugenbefragung mit "+ aktuellerzeuge + " wurde beendet--------------------------------------");
						//ouruserintent=userintent.stop;
						return 7;}
					else if (m8.find()) {
						logger.info("Solve vorgang erbeten");
						//ouruserintent=userintent.solve;
						return 8;}
					else if (m9.find()) {
						logger.info("Bei der Zeugenbefragung wird Watson konsultiert");
						//ouruserintent=userintent.watson;
						return 9;}
			pardon(watson);
			return 10;}//Das ergebnis wurde nicht verstanden

		//tutorial	
		public static void tutorial(int zeit) {
			logger.info("funktion tutorial");
			switch(zeit) {
			case 1://Willst du ein tutorial?
				if(not(yesno(userrequest))) {//tutorial
					tutorial=true;
					tutorialzeit=2;
					speak(tutorial1,"watson");
				}
				else {//keintutorial
					inraum=true;
					tutorial=false;
					speak(einleitung2,"narrator");
					aktuellerraum="entrencehall";
					raum("entrencehall", 1);
					speak(entrencehall[1], "watson");
				}
				break;
			case 2:						//Verstanden?
				if(yesno(userrequest)) {			//tutorial1 verstanden 
					tutorial=true;
					tutorialzeit=3;
					speak(tutorial2,"watson");
				}
				else {								//tutorial1 nicht verstanden
					speak(tutorial1,"watson");
				}	
				break;
			case 3:						//Verstanden?
				if(yesno(userrequest)) {			//tutorial2 nicht verstanden 
					tutorial=true;
					speak(tutorial2,"watson");
				}
				else {								//tutorial2 verstanden 
					tutorialzeit=4;
					aktuellerraum="entrencehall";
					speak(tutorial3,"watson");
				}
			break;
			case 4:						//Verstanden?
				if(yesno(userrequest)) {	//tutorial3 verstanden 
					inraum=true;
					tutorial=false;
					speak(einleitung2,"narrator");
					aktuellerraum="entrencehall";
					raum("entrencehall", 1);
					speak(entrencehall[1], "watson");
				}
				else {						//tutorial3 nicht verstanden 
					tutorialzeit=4;
					aktuellerraum="entrencehall";
					speak(tutorial3,"watson");
					
				}
				break;
			}
			
			return;}

		//zeuge - Funktion, wenn man mit jemandem redet
		public static void zeuge(String name, int zeit) { 
			logger.info("funktion zeuge");
			inbefragung=true;
			String zeugenaussagen[]=new String[5];
			switch (name) {//welcher zeuge wird befragt 
			case "butler": 
				zeugenaussagen=butleraussagen;
				if (butlerbefragt==false) 
					butlerbefragt=true;
				logger.info(name + " wurde beschrieben");
				zeugenzeit=1;
				speak(jamesbeschr,"narrator");
				speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
			case "":
				//Hier müssen noch die Anderen Zeugen hin

			}
			logger.info("funktion zeit");
			switch(zeit){
			case 1://Zeuge wurde beschrieben und es wurde gerade gefragt, welche Frage gestellt werden soll
				int FRAGE=wasfragst(userrequest);//Hier muss noch eine Aussage rein
				//where[0],relation[1],weird[2],susp[3],others[4]
				switch (FRAGE) {
				case 0:
					speak(exampleq, "watson");
				case 1:
					zeugenzeit=2;
					speak(zeugenaussagen[0],name);
					speak("Do you want to ask anything else?","watson");
				case 2:
					zeugenzeit=2;
					speak(zeugenaussagen[1],name);
					speak("Do you want to ask anything else?","watson");
				case 3:
					zeugenzeit=2;
					speak(zeugenaussagen[2],name);
					speak("Do you want to ask anything else?","watson");
				case 4:
					zeugenzeit=2;
					speak(zeugenaussagen[3],name);
					speak("Do you want to ask anything else?","watson");
				case 5:
					zeugenzeit=2;
					speak(zeugenaussagen[4],name);
					speak("Do you want to ask anything else?","watson");
				case 6:
					inbefragung=false;
					inraum=true;
					raumzeit=1;
					zeugenzeit=1;
					speak("Ok, maybe we find something else in this room.","watson");
					//speak(room[1], watson) //erneute beschreibung der Tätigkeiten im Raum
					return;
				case 7:
					//solve();//vielleicht nur eine veränderung in der solvezeit???
				case 8:
					//watson();//vielleicht nur eine veränderung in der watsonzeit???
				case 10:
					pardon(name);
				}
			case 2:
				if(yesno(userrequest)) {			//tutorial1 verstanden 
					zeugenzeit=1;
					speak(frageaufruf,"watson");
					return;
				}
				else {
					inbefragung=false;
					inraum=true;
					raumzeit=1;
					speak("Ok, maybe we'll find something else in this room.","watson");
					return;
				}
			}
			return;}
		
		
		private static boolean not(boolean bool) {
			if (bool) {
				return false;}
			else {
				return true;
			}
		}


		//Funktionen für Räume
		//wastun()
		public static int wastun(String UserRequest) {
			logger.info("funktion was tun");
			UserRequest = UserRequest.toLowerCase();
			String watson="(i would like to |i want to )?(ask )?watson( please)?";
			String search ="(i want to |i would like to )?(search|look)";
			String talk = "(i want to |i would like to )?(ask|talk|question|speak)( to a |to the )?(whitness|victim|butler|maid|gardener|lady|lord|child|cook)?";
			String leave = "(i want to |i would like to )?(leave|exit)";
			String solve = "(i would like to |i want to )?(solve|answer|solution)( the mystery| the game| the murder)?";
			Pattern p1 = Pattern.compile(watson);
			Matcher m1 = p1.matcher(UserRequest);
			Pattern p2 = Pattern.compile(search);
			Matcher m2 = p2.matcher(UserRequest);
			Pattern p3 = Pattern.compile(talk);
			Matcher m3 = p3.matcher(UserRequest);
			Pattern p4 = Pattern.compile(leave);
			Matcher m4 = p4.matcher(UserRequest);
			Pattern p5 = Pattern.compile(solve);
			Matcher m5 = p5.matcher(UserRequest);
			if (m1.find()) {
				logger.info("Watson wird konsultiert.");
				//ouruserintent=userintent.watson;
				return 1;} 
			else if (m2.find()) {
				logger.info("Der Raum wird durchsucht.");
				//ouruserintent=userintent.search;
				return 2;} 
			else if (m3.find()) {
				logger.info("Der Zeuge wird befragt.");
				//ouruserintent=userintent.talk;
				return 3;} 
			else if (m4.find()) {
				logger.info("Der Spieler verlässt den raum");
				//ouruserintent=userintent.leave;
				
				return 4;} 
			else if (m5.find()) {
				logger.info("Der Spieler möchte Solven");
				//ouruserintent=userintent.solve;
				return 5;}
			
			return 10;}

		//raum() ist eine Funktion mit der man Räume besuchen kann.		
		public static void raum(String raum, int zeit) {
			logger.info("Funktion raum started");
			inraum=true;
			aktuellerraum=raum;
			String room[]=new String[4];
			//stellt die richtigen Raumdaten zur verfügen, beschreibt räume, falls nötig
			switch (raum) {
			case "library":
				room=library;
				if (librarybesucht==false) {
					speak(library[0],"narrator");
					librarybesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "lounge":	
				room=lounge;
				if (loungebesucht==false) {
					speak(lounge[0],"narrator");
					loungebesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "garden":	
				room=garden;
				if (gardenbesucht==false) {
					speak(room[0],"narrator");
					gardenbesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "servantwing":	
				room=servantwing;
				if (servantwingbesucht==false) {
					speak(room[0],"narrator");
					servantwingbesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "servantbedroom":	
				room=servantbedroom;
				if (servantbedroombesucht==false) {
					speak(room[0],"narrator");
					servantbedroombesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "bedchamber":	
				room=bedchamber;
				if (bedchamberbesucht==false) {
					speak(room[0],"narrator");
					bedchamberbesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "kitchen":	
				room=kitchen;
				if (kitchenbesucht==false) {
					speak(room[0],"narrator");
					kitchenbesucht=true;
					logger.info(raum + " wurde beschrieben");}

			case "entrencehall":	
				room=entrencehall;
				if (entrencehallbesucht==false) {
					speak(room[0],"narrator");
					entrencehallbesucht=true;
					logger.info(raum + " wurde beschrieben");}
			}
			switch(zeit) {
			case 1:
				int wastun=wastun(userrequest);
				switch (wastun) {
				case 1:
					//watson();
					speak("Ich bin aktuell noch nicht verfügbar","watson");
					break;
				case 2:
					logger.info(raum + " wurde genauer untersucht");
					speak(room[2], "watson");
					speak(room[1], "watson");
					break;
				case 3://zeuge
					aktuellerzeuge=room[4];
					inraum=false;
					inbefragung=true;
					zeuge(aktuellerzeuge, 1);
					break;
				case 4:
					logger.info(raum + " wurde verlassen");
					speak(room[3],"watson");
					//TO DO: Extra case für das verlassen des raumes anlegen
					break;
				case 5:
					//solve();
					break;
				}
				break;
			case 2:
			}

			return;}	

		@Override
		public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope){

			IntentRequest request = requestEnvelope.getRequest();
			Intent intent = request.getIntent();
			userrequest = intent.getSlot("anything").getValue();
			logger.info("Received following text: [" + userrequest + "], on Intent");
			if (tutorial) {
				tutorial(tutorialzeit);
			}
			else if (inbefragung) {
				zeuge(aktuellerzeuge, zeugenzeit);
			} 
			else if (inraum) {
				raum(aktuellerraum, raumzeit);
			}

			else if (insolve) {
				solve(solvezeit);				
			}

			else if (inwatson) {
					watson(watsonzeit);				
			}
			else {
				logger.info("Bei onIntent() gab es Probleme den richtigen Zustand auszuwählen");
			}
			return sende();}





		private void solve(int zeit) {		
			
			return;
		}


		private void watson(int zeit) {
			
			return;
		}


		//andere alexa-Funktionen, die uns nicht interessieren (weil wir sie noch nicht ganz verstehen)


		@Override
		public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
		{
			logger.info("OnLaunch wurde gestartet");
			//SpeechletResponse s=speak("hallo", "narrator");
			
			speak(einleitung1, "narrator");
			logger.info("Einleitung 1 wurde zu text hinzugefügt. "+text);
			speak(tutorialfrage, "watson");
			logger.info("tutorialfrage wurde zu text hinzugefügt. "+text);
			return sende();

			}


		@Override
		public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
			// TODO Auto-generated method stub

		}
		
		private SpeechletResponse askUserResponse(String text)
		{
			SsmlOutputSpeech speech = new SsmlOutputSpeech();
			speech.setSsml("<speak>" + text + "</speak>");

			// reprompt after 8 seconds
			SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
			repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> Bist du noch da?</speak>");

			Reprompt rep = new Reprompt();
			rep.setOutputSpeech(repromptSpeech);

			return SpeechletResponse.newAskResponse(speech, rep);
		}



	}