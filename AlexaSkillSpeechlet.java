/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.customskill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.customskill.AlexaSkillSpeechlet.RecognitionState;
import com.amazon.customskill.AlexaSkillSpeechlet.UserIntent;
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


/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 */


public class AlexaSkillSpeechlet
implements SpeechletV2
{
	
	//Nötiges
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
	private static enum RecognitionState {Zeuge, Raum, YesNo};
	private RecognitionState recState;
	private static enum UserIntent {yes, no, talk, examplequestion, where, suspicion, others, weird, relation, stop, watson, search, leave, solve};
	public static UserIntent ouruserintent;
	public static String userRequest;
	
	//Zustände
	public static boolean befragunglaeuft=false;
	public static boolean inroom=false;
	public static int solveversuche=0;
	public static String murderer="gardener";
	public static String currentroom="entrencehall";
	//Alles über Zeugen

	public static boolean butlerbefragt=false;
	static String jamesbeschr = "The Butler of the house is a grim looking lanky man with pronounced eyebrows and a hooked nose reaching almost down to his lips. He is wearing a worn down, but meticulously cleaned suit.";
	public static String butleraussagen[]=
	//where[0],relation[1],weird[2],susp[3],others[4]
	/*where[0]*/	{"Polishing silver cutlery with the Maid. Why are you asking? Do I seem like a murderer. I have been working here since a long, long time. I’m becoming older, don’t want to risk or lose my job, understand detective?",
	/*relation[1]*/ "Young ‘n stupid man, you know. Had a lot to learn, lately. We Didn’t really have a connection. I was just doing my job here, man.",
	/*weird[2]*/    "A lot of things seem weird these days, don’t  you think? The Old Lady has acted strangely… Bought a lot of stuff. Go ask her and leave me alone.",
	/*susp[3]*/     "Detective, I’m not gonna tell you the secrets of this house. Never have, never will.",
	/*others[4]*/   "I was with the maid. Don’t know anything about the others, mind my own business, you know."};
	//Zusammenfassungen von  butler
	public static String bzsm1 = "";
	public static String bzsm2 = "";
	public static String bzsm3 = "";
	public static String bzsm4 = "";
	public static String bzsm5 = "";
	public static String bzsmf= "";
	
	public static boolean childbefragt=false;
	static String childbeschr = "A little boy, at the age of 6. He has brown hair and on his shirt there is a red blot, possibly cherry sauce. He is the son of the cook";
	static String child1 = "";
	static String child2 = "";
	static String child3 = "";
	//zusammenfassung von child
	String chzsm1 = "";
	String chzsm2 = "";
	String chzsm3 = "";
	String chzsm4 = "";
	String chzsm5 = "";
	public static String chzsmf = "";
	
	public static boolean cookbefragt=false;
	static String cookbeschr = "The cook is probably the friendliest and most loving person in the whole house. She cares for everyone and makes sure everyone gets enough to eat. She also stands for the mistakes of others. She had a very good relationship with the Lord, although the mother hates this.";
	public static String cookaussagen[]=
	//where[0],relation[1],weird[2],susp[3],others[4]
	/*where[0]*/	{"Right here Sir. The Lady wanted to have an extravagant supper. I was cooking, baking and preparing all day for it. I’m still not ready Sir.",
	/*relation[1]*/ "Didn’t really have any. I’m in the kitchen or in the servants wing most of the time. I don’t have enough time for chitchat most of the time. And they treat us as what we are, servants.",
	/*weird[2]*/    "Not that i noticed of. I am focussed on my craft most of the time though.",
	/*susp[3]*/     "Not that i can think of. The Lord payed and treated us well. We are lucky to be here Sir.",
	/*others[4]*/   "I know that my boy was playing in the servant wings. I’ve heard him pretending to be the prince."};
	// Zusammenfassungen von cook
	String czsm1 = "";
	String czsm2 = "";
	String czsm3 = "";
	String czsm4 = "";
	String czsm5 = "";
	public static String czsmf = "";
	
	public static boolean gardenerbefragt=false;
	static String gardenerbeschr = "The gardener is a very simple-minded man. He seems to have been doing his job well for many years. He is said to have been in love with the lady many years ago. Well, in any case he loves his daughter, the maid, more than anything and would probably do anything for her";
	public static String gardeneraussagen[]=
	//where[0],relation[1],weird[2],susp[3],others[4]
	/*where[0]*/	{"I was talking to my daughter, she is the wonderful maid that’s working for the Lord himself. Marvelous girl, if you ask me. Only the best in her mind for everyone. I think she deserved better than being a  maid. I told her to go to the East of the Country and get a better life. But she did not agree with me. I’m sorry, that hasn’t anything to do with your question. ",
	/*relation[1]*/ "I’m only the Gardener, working so that my daughter could have a better life. I’m a gentleman in contrast to the lord, but that’s on a different page. We had a decent relationship. Not to close, but decent.",
	/*weird[2]*/    "Not at all! It’s like it has ever been around here. But if you want to ask a strange man around here, go to the Butler, James. Ask him the questions...It’s such a tragedy what happened. I hope you find the right man, detective.",
	/*susp[3]*/     "James didn’t really like him, but he hasn’t had enemies, so far. I guess everyone liked him but no one knew him for real.",
	/*others[4]*/   "I know my daughter was with me, can’t tell you more. I am sorry for that detective."};
	//zusammenfassung von gardener
	String gzsm1 = "";
	String gzsm2 = "";
	String gzsm3 = "";
	String gzsm4 = "";
	String gzsm5 = "";
	public static String gzsmf = "";
	
	public static boolean ladybefragt=false;
	static String ladybeschr = "The old lady is the mother of the late lord. She is an old woman but very well groomed with lots of make-up and a striking hairstyle. She wears a lot of jewellery and excessive embelishments.";
	public static String ladyaussagen[]=
	//where[0],relation[1],weird[2],susp[3],others[4]
	/*where[0]*/	{"I was in the garden, having a walk and relaxing beneath the willow. I had wanted to try out my new walking dress.",
	/*relation[1]*/ "He was my only child. My baby. His father, my deceased husband, poisoned him with his thoughts of equality. Wanted to be one of the commoners. Said that nobody should have servants. I hoped that he would come to reason. He should’ve listened to me! Commoners will bite of your arm if you offer them a hand. Now he is dead... Well at least I can use his life insurance money to remarry and bury my sorrow in diamonds.",
	/*weird[2]*/    "The shipment of the dresses i bought last week haven’t arrived yet. I need to write them a stern letter if they don’t arrive soon.",
	/*susp[3]*/     "Surely it was one of the dirty servants. He was way too nice to them, paid them way too much too. They probably wanted to extort them for even more money.",
	/*others[4]*/   "I know that the gardner wasn’t in the garden. He get’s me some fruit of the trees usually, but I didn’t see him today."};
	//Zusammenfassungen der lady
	public static String lzsm1 = "";
	public static String lzsm2 = "";
	public static String lzsm3 = "";
	public static String lzsm4 = "";
	public static String lzsm5 = "";
	public static String lzsmf = "";
	
	public static boolean maidbefragt=false;
	static String maidbeschr = "The maid is a shy, young and innocent girl who is satisfied with little. She is not conspicuous but if you look at her for a long time you will notice that she is beautiful. As a prince, you could easily fall for that, don't you think?";
	public static String maidaussagen[]=
	//where[0],relation[1],weird[2],susp[3],others[4]
	/*where[0]*/	{"I was polishing the silver cutlery together with james all day long. He can tell that we did that too! Don’t you dare think I was the murderer of my beloved Lord. ",
	/*relation[1]*/ "I...We… We were good friends. We grew up together, i guess. He is one of the most important persons I have...I had.",
	/*weird[2]*/    "I don't know.. I don't know.. I’m sorry I have to go now…",
	/*susp[3]*/     "I can not think of one person in this whole house who would do this, ever! I mean, James did not really like him but he was polishing the silver cutlery with me, so he’s out. He wasn’t best friends with my father but he would have never done it! NEVER!",
	/*others[4]*/	"I only know me and James were polishing… The cook was cooking all day, for sure. Otherwise I don’t know what everyone does the whole day, why should I?"};
	//zusammenfassung von maid
	String mzsm1 = "";
	String mzsm2 = "";
	String mzsm3 = "";
	String mzsm4 = "";
	String mzsm5 = "";
	public static String mzsmf = "";


//Alles über Räume
//entryhall
	static boolean entrencehallbesucht = false;
	static String entrencehall []=
	/*beschreibung [0]*/	{"The Entrance Hall is dimly lit and feels calm and empty. The eerie silence is only disturbed by the echo of your steps on the cold marble floor and the big doors closing behind you. You see a grande staircase leading up to several doors, a door to the lounge, the kitchen, the bedchamber, the library, the garden and the servants wing. Next to the staircase are two knights armor on either side. A butler is standing next to one of the doors.",
	/*wwyd [1]*/			"What do you want to do in the entrance hall? Would you like to speak to the witness, search the room or leave the room?",
	/*detail [2]*/			"You take a closer look around. Nothing seems unusual to you on the first glance. Everything is perfectly cleaned and there are no traces of an intruder. The murderer must be someone within the house. Then you see it. One of the knights armors positioned next to the staircase is missing it's sword, which could be a murder weapon.",
	/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the library?",
	/*zeuge [4]*/			"butler"};
//library	
	static boolean librarybesucht = false;
	public static String library []=
	/*beschreibung [0]*/	{"WOW! That's a library like it is said in the books. Like bells library from beauty and the beast. High walls and shelves filled with books everywhere around you.  There are even these movable ladders so you can get to the higher shelves that you know from those movies. Also the smell is as you always imagined it. There are comfortable armchairs that invite you to linger and in the background ist soft music playing. There's no one to be seen here far and wide... But there's something moving back there, isn't it? Ahh, a cat! It looks sweet and fluffy, do you want to pet it?",
	/*wwyd [1]*/			"What do you want to do in the library? Would you like to search the room, pet the cat or leave the room?",
	/*detail [2]*/			"You see that the cat was treated better than the servants. It has a red bow of silk tied around it's neck and the porcelain bowls in the corner are full of fresh meat and water. Someone must tend to it heartwarmingly.",
	/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the entrencehall?",
	/*zeuge [4]*/			"cat"};
//garden	
	static boolean gardenbesucht = false;
	public static String garden []=
	/*beschreibung [0]*/	{"You enter the garden and can hardly believe your eyes. A large weeping willow surrounded by the most beautiful flowerbeds you have ever seen is in the center of the large field. An old broken bench looks grim underneath the willow. A small pond next to the tree reflects the silver moonlight. Fruit trees frame a narrow path, which you follow down to a greenhouse. Bent over a vegetable patch, you see a gardener, an old man with soft friendly features. When he notices you, he turns around and smiles encouragingly at you.",
	/*wwyd [1]*/			"What do you want to do in the Garden? Would you like to speak to the witness, search the room or leave the room?",
	/*detail [2]*/			"You see that the Gardner's forehead is covered in sweat, but you aren't sure whether it's from the hard work whether he is hiding something. ",
	/*whroom [3]*/			"Where do you want to go: The entrencehall? The kitchen? The lounge? The servantwing? The Servantbedroom? The bedchamber or the library?",
	/*zeuge [4]*/			"gardener"};
//servantbedroom	
	static boolean servantbedroombesucht = false;
	public static String servantbedroom []=
	/*beschreibung [0]*/	{"You've come through the servants' wing and you see an open door, you look in. Here, compared to the hallway, it looks really nice and cosy, despite the uncomfortable walls surrounding the room. There is a small bed, a chest of drawers with a mirror and a narrow closet in the room. A girl is sitting on the stool in front of the chest of drawers, you think that it can only be the maid. She holds a photo in her hand. When she notices you, she quickly lets it disappear, but you catch a last glimpse of it. It's a photo of the deceased lord with the maid...What was the connection between these two? She looks frightened and at the same time infinitely sad. You start asking her a few questions. ",
	/*wwyd [1]*/			"What do you want to do in the servants bedroom? Would you like to speak to the witness, search the room or leave the room?",
	/*detail [2]*/			"The girl is clearly madly in love with the Lord. Was the relationship one sided or was he involved too? She looks fairly innocent, you don't think that she could've commited such a gruesome murder. Maybe an act of jealousy?",
	/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The servants wing? The entrencehall? The bedchamber or the library?",
	/*zeuge [4]*/			"maid"};
//kitchen	
	static boolean kitchenbesucht = false;
	public static String kitchen []= 
	/*beschreibung [0]*/	{"You're standing in the heart of the house now, here's the kitchen. It smells and bubbles everywhere. It scents like freshly cooked food. On the wall are pots and pans hanging and you hear dishes clattering. You look to the right and see a large table full of delicacies: Sliced fruit, vegetables, various sauces and bowls with meat and potatoes and much more. Next to them are a lot of fresh herbs. The cook also stands here and tries to place more food on the table. ",
	/*wwyd [1]*/			"What do you want to do in the kitchen? Would you like to speak to the witness, search the room or leave the room?",
	/*detail [2]*/			"You look around and notice that the table is so full of food that there isn't a single empty square inch of free space. This amount would take hours, maybe even days to cook. You also notice that several knifes are missing from the knife block.",
	/*whroom [3]*/			"Where do you want to go: The garden? The entrencehall? The lounge? The servants wing? The Servants bedroom? The bedchamber or the library?",
	/*zeuge [4]*/			"cook"};
//servantwing	
	final boolean servantwingbesucht = false;
	public final String servantwing []= 
	/*beschreibung [0]*/	{"You get into a dark corridor with bare walls, a single flickering light bulb hangs from the ceiling. The wooden planks under your feet creak and the air is cold and somehow damp. This is by far the creepiest room in the castle. But you hear another sound, what is that? It's the kitchen boy playing on the floor. He has some wooden toys that have the best behind them. But he looks quite happy the way he sits there. When he sees you he is frightened, but the creaking planks should have announced you? Strange. He crouches down a bit, you calm him down and ask why he is afraid...",
	/*wwyd [1]*/				"What do you want to do in the servants wing? Would you like to speak to the witness, search the room or leave the room?",
	/*detail [2]*/			"You don't notice anything exceptional in corridor. It's cold and damp like every other servant corridor. People don't care whether servants are comfortable or not.",
	/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrencehall? The Servantbedroom? The bedchamber or the library?",
	/*zeuge [4]*/			"child"};
//bedchamber	
	static boolean bedchamberbesucht = false;
	public static String bedchamber []= 
	/*beschreibung [0]*/	{"You open the door into a luxurious Bedroom. Floorlength Windows almost hidden beneath heavy velvet curtains shine silver light into the spacious room which is full of handcarved furnishings. Detailed paintings in golden Frames decorate every inch of the walls and a big carpet covers the finely polished parquet. An elderly Lady strutts absently through the room.One expensive garment after another disappears into the already empty closet of the recently deceased Lord. When she notices you, she looks frightened for a split second though she catches herself quickly and starts sobbing without shedding a tear. She grabs your arm and looks out of the window, all in one dramatic gesture.",
	/*wwyd [1]*/			"What do you want to do in the Bed chamber? Ask the withness, leave the room or solve?",
	/*detail [2]*/			"You see that the Lord's mother, the lady, isnt as destressed with the death of her son as she is trying to convey. She has already cleaned out every single piece of her sons clothing from his bedroom.",
	/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrencehall? The Servantbedroom? The servantwing or the library?",
	/*zeuge [4]*/			"lady"};
//lounge	
	static boolean loungebesucht = false;
	public static String lounge []= 
	/*beschreibung [0]*/	{"As you enter the scene of the crime thunder disturbs the gloomy night. You see a fireplace which fire has gone out a long time ago. Two leather armchairs and a small table opposite of the fireplace seem to have been pushed aside. Expensive liquor that once was on the table now was spilled all over the carpet. The body of the deceased is laying behind the armchair, as if he tried to create distance between him and his murderer. His eyes are still wide-open in fear. His elegant clothing is now drenched in his own blood coming out of a single stab wound in the middle of his chest. You close his eyes.",
	/*wwyd [1]*/			"What do you want to do in the Lounge? Would you like to search the room or leave the room?",
	/*detail [2]*/			"When you take a closer look you see that one corner of the carpet is folded under itself. Someone could easily trip over it. ",
	/*whroom [3]*/			"Where do you want to go: The kitchen? The garden? The entrencehall? The servants wing? The Servants bedroom? The bedchamber or the library?",
	/*zeuge [4]*/			"lord"};
	
//Alle Erzählertexte
//Intro
public static String einleitung1 = "It was a dark and stormy night, when you and your assistant Watson were invited to Furbish Manor. The rain hammered on the roof of the cab you were using to get there. You stand in front of the dark, iron gate and wait that somebody comes to let you in. The wind is cold and you have to wait a moment in this unpleasant place. Watson looks at you: “Do you remember how we normaly solve a case, or should i explain to you how we do this?";
public static String einleitung2 = "After a few minutes finally somebody opens the gate to let you in. Together you walk the street up the hill until you come to the entry. The door opens and you enter.";

//Tutorial
public static String tutorial1 = "Its really not that complicated: We walk around a house and ask the people we meet the questions we want to ask them. They answer. Most often they have seen something or have not seen something in a while. You should ask them for their alibis and who can prove them. Maybe somebody is lying, so you should listen carefully. Did you understand that?";
public static String tutorial2 = "If we enter the room you will get a general description of what is in it. You can look at some things, if you want to learn more about them. Should i repeat what i just said?";
public static String tutorial3 = "If you forget anything that you have found you can always ask me. Just say “Watson” and i will be there. I can give you a short summary and some more tipps. Did you understand me? The rain is so loud, i can understand if you say, that you did not.";

//Watson
	//Interogation
	public static String questionsmore = "Anything else?";
	public static String frageaufruf = "What would you like to ask? If you need some example-questions you can ask me for some";
		//help
		static String watson = "How can i help you? I can give you a wrap up of all we know so far. I can also tell you some vocabulary you do not know.";
		static String watsonzsm = "Ok, here is what we know so far."+ bzsmf + gzsmf + czsmf + lzsmf + mzsmf + chzsmf + "Thats it. Do you want to hear it again?";
		static String watsonmore = "Can i help you some more?";
		//example questions
		public static String exampleq = "The main things you need to find out to solve the mystery successfully are where the people have been when the crime has happened, if they have any suspicion who could have done such terrible things, if something did seem weird to them lately. Furthermore, you should ask the persons about their relationships with the deceased Lord and maybe if they knew some things about the other persons living there and if they could have done the crime.Do you want to listen to a few example questions and if so, which?";
		public static String exampleqwhich = "Choose to what you want to hear an example question: Where, relationship, weirdness, suspicion, other persons.";
		public static String question1 = "Where have you been when the crime was committed?";
		public static String question2 = "What was your relationship with the deceased Lord?";
		public static String question3 = "Did something seem weird to you lately?";
		public static String question4 = "Do you have any suspicion?";
		public static String question5 = "Could you imagine of of the others committing such a crime?";
		public static String exampleqmore = "Do you want to listen to further example questions?";
	
//Solving
	public static String solvequ = "You think you solved it? Have you asked enough people? Looked at every clue?";
	public static String solvewho = "I am impressed. Who do you think is the murderer?";
	public static String solveright = "You made it. You found the murderer of Lord Furbish. It was indeed the gardener. Let's see what he has to say.";
	public static String solvemono = "My poor sweet daughter, the maid, had an affair with Lord Furbish. The Lord tarnished her honor and their relationship was doomed from the start. I took the sword and threatened him so he would leave my daughter in peace. Then I tripped over this damned carpet. The sword pierced his chest and he was dead within seconds."; 
	public static String solvewrong = "No, it seems it was not this witness either.";
	public static String solveanothertry = "Do you want another try?";
	public static String solveescape = "And even worse: I fear that the real murderer has escaped.";

	
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins");
		
	}
	
	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		
	return;}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope){
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		String userRequest = intent.getSlot("anything").getValue();//habe String hinzugefügt
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		Object resp = null;
		switch (recState) {
		case Zeugen: resp = wasfragst(userRequest, zeuge); break;
		case Raum: resp = wastun(userRequest, raum); break;
		case YesNo: resp = yesno(userRequest); break;
		default: resp = response("Erkannter Text: " + userRequest);//was tun, wenn user intent nicht erkannt wurde
		}   
	return resp;}
			
	public static SpeechletResponse speak(String text, String name) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		switch(name){ 
		case "narrator": 
			speech.setSsml("<speak><voice name=\"Matthew\"><lang xml:\"en-US\">" + text + "</lang></voice></speak>");
			break; 
		case "watson": 
			speech.setSsml("<speak><voice name=\"Brian\"><lang xml:\"en-GB\">" + text + "</lang></voice></speak>");
			break; 
		case "maid": 
			speech.setSsml("<speak><voice name= \"Kimberly\"><lang xml:\"en-US\">" + text + "</lang></voice></speak>");
			break; 
		case "gardener": 
			speech.setSsml("<speak><voice name= \"Russel\"><lang xml:\"en-AU\">" + text + "</lang></voice></speak>");
			break; 
		case "butler": 
			speech.setSsml("<speak><voice name= \"Geraint\"><lang xml:\"en-GB-WLS\">" + text + "</lang></voice></speak>");
			break;
		case "cook": 
			speech.setSsml("<speak><voice name= \"Kendra\"><lang xml:\"en-US\">" + text + "</lang></voice></speak>");
			break;
		case "child": 
			speech.setSsml("<speak><voice name= \"Justin\"><lang xml:\"en-US\">" + text + "</lang></voice></speak>");
			break;
		case "lady": 
			speech.setSsml("<speak><voice name= \"Amy\"><lang xml:\"en-GB\">" + text + "</lang></voice></speak>");
			break;
		case "cat":
			int randomNum = ThreadLocal.Random.current().nextInt(1,4);
			if (randomNum == 1){
				speech.setSsml("<speak>"+text+"<audio src="soundbank://soundlibrary/animals/amzn_sfx_cat_long_meow_1x01"/></speak>");
				} else if {
					randomNum==2 { 
						speech.setSsml(<speak>+text+<audio src="soundbank://soundlbrary/animals/amzn_sfx_cat_purr_meow_01"/></speak>);
					} else {
						speech.setSsml(<speak>+text+<audio src="soundbank://soundlbrary/animals/amzn_sfx_cat_purr_03"/></speak>);
					}}
			break;
		case "lord":
			import java.util.concurrent.ThreadLocalRandom;
			int randomInt = ThreadLocal.Random.current().nextInt(1,4);
			if randomInt == 1{
				speech.setSsml(<speak>+text+<audio src="soundbank://soundlibrary/clocks/ticks/ticks_04"/></speak>);
				} else if {
					randomInt==2 { 
						speech.setSsml(<speak>+text+<audio src="soundbank://soundlbrary/nature/amzn_sfx_rain_on_roof_01"/></speak>);
					} else {
						speech.setSsml(<speak>+text+<audio src="soundbank://soundlibrary/home/amzn_sfx_footsteps_muffled_02"/></speak>);
					}}
			break;
		default: 
			speech.setSsml(<speak><voice name= "Joey"><lang xml:"en-US"> + text + </lang></voice></speak>);
		} 

		return SpeechletResponse.newTellResponse(speech);
	}
			
		//pardon() ist eine funktion, falls etwas nicht verstanden wurde
			public static void pardon(String name) {
				speak("sorry, could you repeat that?", name);
			return;}
			
		//solve() ist die Auflösefunktion
			public static void solve() {
				//solveversuche = Variable die versuche speichert.
			return;}
		
		//watson() ist der aufruf unseres freundliche Helfers
			public static void watson() {
				
			return;}
		
		//yesno():
			public static void yesno(String UserRequest) {
				String yes = "*?yes*?";
				String no = "*?no*?";
				Pattern p1 = Pattern.compile(yes);
				Matcher m1 = p1.matcher(UserRequest);
				Pattern p2 = Pattern.compile(no);
				Matcher m2 = p2.matcher(UserRequest);
				if (m1.find()) {
					ouruserintent=UserIntent.yes;} 
				else if (m2.find()) {
					ouruserintent=UserIntent.no;} 
			logger.info("Es gab einen fehler bei Yesno");
			return;}
						
	//Funktionen zur Zeugenbefragung
		//answer: beantwortet eine Frage {where[0]relation[1],weird[2],susp[3]*,others[4]}, mit der Stimme von Name
			public static void answer(int Frage, String[] aussage, String name) {
				speak(aussage[Frage],name);
			return;}
			
		//wasfragst - Funktion um herauszufinden, was der User fragt
			public int wasfragst(String userRequest) {
				userRequest = userRequest.toLowerCase();
				String beispielfrage = "(i )(need |could use |want)(some |a |many )?(example questions|example(s)?|help|question(s)?)";
				String where = "tell me about your whereabouts | where have you been(, when the murder (occured|happened)|, when the lord was murdered)";
				String relation = "*?(relation|love|hate|stand to)*?";
				String weird = "*?(weird|strange)";
				String suspicion = "(do you have an(y)? (suspicion| idea |theory ) who (might (have done it|be the murderer)|(did it| have comitted the murder(of lord furbish|the lord)?))|Who killed (him|lord furbish)";
				String others = "(gardener |cook |lady |child |maid |butler)*?";
				String stop = "i (don't |do not )(want to |would like to )stop(the interrogation|the questioning|asking)";
				String solve = "*?solve*?";
				String watson = "*?watson*?";
				Pattern p1 = Pattern.compile(beispielfrage);
				Matcher m1 = p1.matcher(userRequest);
				Pattern p2 = Pattern.compile(where);
				Matcher m2 = p2.matcher(userRequest);
				Pattern p3 = Pattern.compile(relation);
				Matcher m3 = p3.matcher(userRequest);
				Pattern p4 = Pattern.compile(weird);
				Matcher m4 = p4.matcher(userRequest);
				Pattern p5 = Pattern.compile(suspicion);
				Matcher m5 = p5.matcher(userRequest);
				Pattern p6 = Pattern.compile(others);
				Matcher m6 = p6.matcher(userRequest);
				Pattern p7 = Pattern.compile(stop);
				Matcher m7 = p7.matcher(userRequest);
				Pattern p8 = Pattern.compile(solve);
				Matcher m8 = p8.matcher(userRequest);
				Pattern p9 = Pattern.compile(watson);
				Matcher m9 = p9.matcher(userRequest);
				if (m1.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach Beispielfragen gefragt");
					ouruserintent=UserIntent.examplequestion;} 
				else if (m2.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach dem Aufenthaltsortgefragt");
					ouruserintent=UserIntent.where;} 
				else if (m3.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach Beziehungen gefragt");
					ouruserintent=UserIntent.relation;} 
				else if (m4.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach Merkwürdigem gefragt");
					ouruserintent=UserIntent.weird;} 
				else if (m5.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach Verdächtigungen gefragt");
					ouruserintent=UserIntent.suspicion;;}
				else if (m6.find()) {
					logger.info("Bei der Zeugenbefragung wurde nach den Anderen gefragt");
					ouruserintent=UserIntent.others;}
				else if (m7.find()) {
					logger.info("Die Zeugenbefragung wurde beendet--------------------------------------");
					ouruserintent=UserIntent.stop;}
				else if (m8.find()) {
					logger.info("Solve vorgang erbeten");
					ouruserintent=UserIntent.solve;}
				else if (m9.find()) {
					logger.info("Bei der Zeugenbefragung wird Watson konsultiert");
					ouruserintent=UserIntent.watson;}
			return 10;}//Das ergebnis wurde nicht verstanden
			
		//talk to - Funktion um mit jemandem zu reden
			public static void talkto(String name) { 
				logger.info(name + " wird jetzt befragt -------------------------");
				befragunglaeuft=true;
				String zeugenaussagen[]=new String[5];
				switch (name) {//welcher zeuge wird befragt 
					case "butler": 
						zeugenaussagen=butleraussagen;
						if (butlerbefragt==false) 
							speak(jamesbeschr,"narrator");
							butlerbefragt=true;
							logger.info(name + " wurde beschrieben");}
						
						speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
						while (befragunglaeuft==true){
							int FRAGE=wasfragst(UserRequest);//Hier muss noch eine Aussage rein
							//where[0],relation[1],weird[2],susp[3],others[4]
							switch (FRAGE) {
								case 0:
									speak(exampleq, "watson");
								case 1:
									speak(zeugenaussagen[0],name);
								case 2:
									speak(zeugenaussagen[1],name);
								case 3:
									speak(zeugenaussagen[2],name);
								case 4:
									speak(zeugenaussagen[3],name);
								case 5:
									speak(zeugenaussagen[4],name);
								case 6:
									befragunglaeuft=false;
									return;
								case 7:
									solve();
								case 8:
									watson();
								case 10:
									pardon(name);
							}
						}
				return;}
		
	//Funktionen für Räume
			//wastun()
				public static void wastun(String UserRequest) {
					UserRequest = UserRequest.toLowerCase();
					String watson="";
					String search ="";
					String talk = "";
					String leave = "";
					String solve = "";
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
						ouruserintent=UserIntent.watson;} 
					else if (m2.find()) {
						logger.info("Der Raum wird durchsucht.");
						ouruserintent=UserIntent.search;} 
					else if (m3.find()) {
						logger.info("Der Zeuge wird befragt.");
						ouruserintent=UserIntent.talk;} 
					else if (m4.find()) {
						logger.info("Der Spieler verlässt den raum");
						ouruserintent=UserIntent.leave;} 
					else if (m5.find()) {
						ouruserintent=UserIntent.solve;}
				return;}
			
			//visit() ist eine Funktion mit der man Räume besuchen kann.		
				public static void	visit(String raum) {
					logger.info(raum + " wurde betreten");
					inroom=true;
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
					while(inroom) {
						speak(room[1], "watson");
						
						int wastun=wastun(UserRequest);
						switch (wastun) {
							case 0:
								watson();
							case 2:
								speak(room[2], "watson");
								logger.info(raum + " wurde genauer untersucht");
							case 3:
								talkto(room[4]);
							case 4:
								inroom=false;
								logger.info(raum + " wurde verlassen");
								visit(room[3]);
							case 5:
								solve();
						}
					}
					
				return;}	
			
			
			
			
			
			
		}
/*	
	public static String userRequest;
	private static int sum;
	private static String answerOption1 = "";
	private static String answerOption2 = "";
	private static String question = "";
	private static String correctAnswer = "";
	
	//was macht das??
	private static enum RecognitionState {Answer, YesNo};
	private RecognitionState recState;
	private static enum UserIntent {Yes, No, A, B, C, D, Publikum, FiftyFifty};
	UserIntent ourUserIntent;
		
	private String buildString(String msg, String replacement1, String replacement2) {
		return msg.replace("{replacement}", replacement1).replace("{replacement2}", replacement2);
	}

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("Alexa session begins");
		recState = RecognitionState.Answer;

		
		
	}
	
	
	
	

	}
	
	
	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		selectQuestion();
		return askUserResponse(welcomeMsg+" "+question);
	}


	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		userRequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		switch (recState) {
		case Answer: resp = evaluateAnswer(userRequest); break;
		case YesNo: resp = evaluateYesNo(userRequest); recState = RecognitionState.Answer; break;
		default: resp = response("Erkannter Text: " + userRequest);
		}   
		return resp;
	}
	
	private SpeechletResponse evaluateYesNo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			selectQuestion();
			res = askUserResponse(question); break;
		} case No: {
			res = response(buildString(sumMsg, String.valueOf(sum), "")+" "+goodbyeMsg); break;
		} default: {
			res = askUserResponse(errorYesNoMsg);
		}
		}
		return res;
	}



*/
	/**
	 * formats the text in weird ways
	 * @param text
	 * @param i
	 * @return
	 */
/*	private SpeechletResponse responseWithFlavour(String text, int i) {

		SsmlOutputSpeech speech = new SsmlOutputSpeech();
		switch(i){ 
		case 0: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
			break; 
		case 1: 
			speech.setSsml("<speak><emphasis level=\"strong\">" + text + "</emphasis></speak>");
			break; 
		case 2: 
			String half1=text.split(" ")[0];
			String[] rest = Arrays.copyOfRange(text.split(" "), 1, text.split(" ").length);
			speech.setSsml("<speak>"+half1+"<break time=\"3s\"/>"+ StringUtils.join(rest," ") + "</speak>");
			break; 
		case 3: 
			String firstNoun="erstes Wort buchstabiert";
			String firstN=text.split(" ")[3];
			speech.setSsml("<speak>"+firstNoun+ "<say-as interpret-as=\"spell-out\">"+firstN+"</say-as>"+"</speak>");
			break; 
		case 4: 
			speech.setSsml("<speak><audio src='soundbank://soundlibrary/transportation/amzn_sfx_airplane_takeoff_whoosh_01'/></speak>");
			break;
		default: 
			speech.setSsml("<speak><amazon:effect name=\"whispered\">" + text + "</amazon:effect></speak>");
		} 

		return SpeechletResponse.newTellResponse(speech);
	}


	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		logger.info("Alexa session ends now");
	}

*/

	/**
	 * Tell the user something - the Alexa session ends after a 'tell'
	 */
/*
private SpeechletResponse response(String text)
	{
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}
*/
	/**
	 * A response to the original input - the session stays alive after an ask request was send.
	 *  have a look on https://developer.amazon.com/de/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html
	 * @param text
	 * @return
	 */
/*
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
*/