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
//GLOBALE VARIABLEN__________________________________________________________________________________________________________________________________	
	//All die Variablen, die bei Durchführung des Programms benötigt werden.
	//Nötiges----------------------------------------------------------------------------------------------------------------------------------------
			static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
			public static String text="<speak>"; //dies ist die Variable, die von speak gefüllt und dann durch send() gesendet wird
			public static String murderer="gardener"; //Hier lässt sich anpassen wer der Mörder ist. Dann sollte natürlich auch die Geschichte angepasst werden.
			public static int fehleranzahl=0;
			public static String fehler="Es gab folgende nicht erkannte Eingaben:";
	//Zustände---------------------------------------------------------------------------------------------------------------------------------------
			//Alles was dem Programm hilft wo wir mit wem gesprochen haben.
			public static String aktuellerzeuge= " ";
			public static String aktuellerraum = " ";
			public static String room[]=new String[4];
			public static String zeugenaussagen[]=new String[5];
			
			public static int tutorialzeit=1;
			public static int solvezeit=1;
			public static int watsonzeit=1;
			public static int zeugenzeit=1;
			public static int raumzeit=1;
			private static boolean intutorial=true;
			private static boolean insolve=false;
			private static boolean inwatson=false;
			private static boolean inbefragung=false;
			private static boolean inraum=false;
			//userintents
				private static enum userintent {yes, no, talk, examplequestion, where, suspicion, others, weird, relation, stop, watson, search, leave, solve};
				public static userintent ouruserintent;
				public static String userrequest;	

	//Texte------------------------------------------------------------------------------------------------------------------------------------------
				//Alle gesprochenen Texte. Bei neuen Fällen mussen neue Texte hier rein.
				public static String frageaufruf="what do you want to ask this witness? If you need some example questions, ask me.";
				public static String exampleq = "The main things you need to find out to solve the mystery successfully are where the people have been when the crime has happened, if they have any suspicion who could have done such terrible things or if something did seem weird to them lately. Furthermore, you should ask the persons about their relationships with the deceased Lord and maybe if they knew some things about the other persons living there and if they could have done the crime.";//Die Examplequestions müssen rein
		//Einleitung  --  Bei neuer Einleitung, kann dieser Text erweitert werden
				public static String einleitung1 = "It was a dark and stormy night, when you and your assistant Watson were invited to Furbish Manor. The rain hammered on the roof of the cab you were using to get there. You stand in front of the dark iron gate and wait that somebody comes to let you in. The wind is cold and you have to wait a moment in this unpleasant place. Watson looks at you" ;
				public static String tutorialfrage="Do you remember how we normaly solve a case? Yes or No?";
				public static String einleitung2 = "After a few minutes finally somebody opens the gate to let you in. Together you walk the street up the hill until you come to the entry. The door opens and you enter.";
		//Tutorial   --  Das Tutorial kann angepasst werden, da die Funktionen aber gleich bleiben, kann man es auch hierbei belassen. ACHTUNG: Hier sind einige gepolte Fragen: Wenn man nicht in tutorial() etwas ändern möchte, sollten die Fragen so gestellt bleiben.
				public static String tutorial1 = "Its really not that complicated: We walk around a house and ask the people we meet the questions we want to ask them. They answer. You could ask them, where they have been or if they have seen something strange. You should ask them for their alibis and who can prove them. Maybe somebody is lying, so you should listen carefully. Did you understand that? Yes or no?";
				public static String tutorial2 = "If we enter the room you will get a general description of what is in it. You can then search, if you want to learn more about it. Should i repeat what i just said? Yes or no?";
				public static String tutorial3 = "If you forget anything that you have found you can always ask me. Just say “Watson” and i will be there. I can give you a short summary and some more tipps. If you think, that you solved this case: Just say that you want to solve it. Did you understand me?";
			
		//Alles über Räume
				//Um einen weiteren Raum hinzuzufügen, muss man diesen sowohl in raumregex[] einfügen, als auch im raumarray. Außerdem benötigt es den bool <NamedesRaums>, sowie eine Array Raumbeschreibung im gleichen Muster wie die anderen unterhalb.
				public static String[] raumregex= {"((entrancehall)|(entrencehall)|(entry)|(knights armor)|(butler))","(library|cat)","((garden(er)?))","(servantbedroom|maid)","(kitchen|cook)","(basement|child)","(bedchamber|lady)","(lounge|deceased|lord)"};
				public static String[] raumarray= {"entrancehall","library","garden","servantbedroom","kitchen", "basement", "bedchamber","lounge"};
				//entryhall
					static boolean entrancehallbesucht = false;
					static String entrancehall []=
								/*beschreibung [0]*/	{"The Entrance Hall is dimly lit and feels calm and empty. The eerie silence is only disturbed by the echo of your steps on the cold marble floor and the big doors closing behind you. You see a grande staircase leading up to several doors, a door to the lounge, the kitchen, the bedchamber, the library, the garden and the basement. Next to the staircase are two knights armor on each side. A butler is standing next to one of the doors.",
								/*wwyd [1]*/			"What do you want to do in the entrance hall? Would you like to speak to the witness, search the room or leave the room?",
								/*detail [2]*/			"You take a closer look around. Nothing seems unusual to you on the first glance. Everything is perfectly cleaned and there are no traces of an intruder. The murderer must be someone within the house. Then you see it. One of the knights armors positioned next to the staircase is missing it's sword, which could be a murder weapon.",
								/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The basement? The Servantbedroom? The bedchamber or the library?",
								/*zeuge [4]*/			"butler"};
				//library	
					static boolean librarybesucht = false;
					public static String library []=
								/*beschreibung [0]*/	{"WOW! That's a library like it is said in the books. High walls and shelves filled with books everywhere around you.  There are even these movable ladders so you can get to the higher shelves that you know from those movies. Also the smell is as you always imagined it. There are comfortable armchairs that invite you to linger and in the background ist sof music playing. There's no one to be seen here far and wide... But there's something moving back there, isn't it? Ahh, a cat! It looks sweet and fluffy.",
								/*wwyd [1]*/			"What do you want to do in the library? Would you like to search the room, talk to the witness or leave the room?",
								/*detail [2]*/			"You see that the cat was treated better than the servants. It has a red bow of silk tied around it's neck and the porcelain bowls in the corner are full of fresh meat and water. Someone must tend to it heartwarmingly.",
								/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The basement? The Servantbedroom? The bedchamber or the entrancehall?",
								/*zeuge [4]*/			"cat"};
				//garden	
					static boolean gardenbesucht = false;
					public static String garden []=
								/*beschreibung [0]*/	{"You can hardly believe your eyes. A large weeping willow surrounded by the most beautiful flowerbeds you have ever seen is in the center of the large field. An old broken bench looks grim underneath the willow. A small pond next to the tree reflects the silver moonlight. Fruit trees frame a narrow path, which you follow down to a greenhouse. Bent over a vegetable patch, you see a gardener, an old man with soft friendly features. When he notices you, he turns around and smiles encouragingly at you.",
								/*wwyd [1]*/			"What do you want to do in the Garden? Would you like to speak to the witness, search the garden or leave the garden?",
								/*detail [2]*/			"You see that the Gardner's forehead is covered in sweat, but you aren't sure whether it's from the hard work or he is hiding something. ",
								/*whroom [3]*/			"Where do you want to go: The entrancehall? The kitchen? The lounge? The basement? The Servantbedroom? The bedchamber or the library?",
								/*zeuge [4]*/			"gardener"};
				//servantbedroom	
					static boolean servantbedroombesucht = false;
					public static String servantbedroom []=
								/*beschreibung [0]*/	{"You've come through the basement and you see an open door, you look in. Here, compared to the hallway, it looks really nice and cosy, despite the uncomfortable walls surrounding the room. There is a small bed, a chest of drawers with a mirror and a narrow closet in the room. A girl is sitting on the stool in front of the chest of drawers, you think that it can only be the maid. She holds a photo in her hand. When she notices you, she quickly lets it disappear, but you catch a last glimpse of it. It's a photo of the deceased lord with the maid...What was the connection between these two? She looks frightened and at the same time infinitely sad. ",
								/*wwyd [1]*/			"What do you want to do in the servants bedroom? Would you like to speak to the maid, search the room or leave the room?",
								/*detail [2]*/			"The girl is clearly madly in love with the Lord. Was the relationship one sided or was he involved too? She looks fairly innocent, you don't think that she could've commited such a gruesome murder. Maybe an act of jealousy?",
								/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The basement? The entrancehall? The bedchamber or the library?",
								/*zeuge [4]*/			"maid"};
				//kitchen	
					static boolean kitchenbesucht = false;
					public static String kitchen []= 
								/*beschreibung [0]*/	{"You're standing in the heart of the house now, here's the kitchen. It smells and bubbles everywhere. It scents like freshly cooked food. On the wall are pots and pans hanging and you hear dishes clattering. You look to the right and see a large table full of delicacies: Sliced fruit, vegetables, various sauces and bowls with meat and potatoes and much more. Next to them are a lot of fresh herbs. The cook also stands here and tries to place more food on the table.",
								/*wwyd [1]*/			"What do you want to do in the kitchen? Would you like to speak to the witness, search the room or leave the room?",
								/*detail [2]*/			"You look around and notice that the table is so full of food that there isn't a single empty square inch of free space. This amount would take hours, maybe even days to cook. You also notice that several knifes are missing from the knife block.",
								/*whroom [3]*/			"Where do you want to go: The garden? The entrancehall? The lounge? The basement? The Servants bedroom? The bedchamber or the library?",
								/*zeuge [4]*/			"cook"};
				//basement
					public static boolean basementbesucht = false;
					public static String basement []= 
								/*beschreibung [0]*/	{"You get into a dark corridor with bare walls, a single flickering light bulb hangs from the ceiling. The wooden planks under your feet creak and the air is cold and somehow damp. This is by far the creepiest room in the castle. But you hear another sound, what is that? It's the kitchen boy playing on the floor. He has some wooden toys that have the best behind them. But he looks quite happy the way he sits there. When he sees you he is frightened, but the creaking planks should have announced you? Strange.",
								/*wwyd [1]*/				"What do you want to do in the basement? Would you like to speak to the witness, search the room or leave the room?",
								/*detail [2]*/			"You don't notice anything exceptional in corridor. It's cold and damp like every other servant corridor. People don't care whether servants are comfortable or not.",
								/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrancehall? The Servantbedroom? The bedchamber or the library?",
								/*zeuge [4]*/			"child"};
				//bedchamber	
					public static boolean bedchamberbesucht = false;
					public static String bedchamber []= 
								/*beschreibung [0]*/	{"You open the door into a luxurious Bedroom. Floorlength Windows almost hidden beneath heavy velvet curtains shine silver light into the spacious room which is full of handcarved furnishings. Detailed paintings in golden Frames decorate every inch of the walls and a big carpet covers the finely polished parquet. An elderly Lady strutts absently through the room.One expensive garment after another disappears into the already empty closet of the recently deceased Lord. When she notices you, she looks frightened for a split second though she catches herself quickly and starts sobbing without shedding a tear. She grabs your arm and looks out of the window, all in one dramatic gesture.",
								/*wwyd [1]*/			"What do you want to do in the Bed chamber? Ask the withness,search the room or leave the room?",
								/*detail [2]*/			"You see that the Lord's mother, the lady, isnt as destressed with the death of her son as she is trying to convey. She has already cleaned out every single piece of her sons clothing from his bedroom.",
								/*whroom [3]*/			"Where do you want to go: The garden? The kitchen? The lounge? The entrancehall? The Servantbedroom? The basement or the library?",
								/*zeuge [4]*/			"lady"};
				//lounge	
					public static boolean loungebesucht = false;
					public static String lounge []= 
								/*beschreibung [0]*/	{"As you enter the scene of the crime thunder disturbs the gloomy night. You see a fireplace which fire has gone out a long time ago. Two leather armchairs and a small table opposite of the fireplace seem to have been pushed aside. Expensive liquor that once was on the table now was spilled all over the carpet. The body of the deceased is laying behind the armchair, as if he tried to create distance between him and his murderer. His eyes are still wide-open in fear. His elegant clothing is now drenched in his own blood coming out of a single stab wound in the middle of his chest. ",
								/*wwyd [1]*/			"What do you want to do in the Lounge? Would you like to search the room or leave the room?",
								/*detail [2]*/			"When you take a closer look you see that one corner of the carpet is folded under itself. Someone could easily trip over it. ",
								/*whroom [3]*/			"Where do you want to go: The kitchen? The garden? The entrancehall? The basement? The Servants bedroom? The bedchamber or the library?",
								/*zeuge [4]*/			"lord"};
			
		//Alles über Zeugen
				//Ein Zeuge besteht aus einem Boolean z.B. butlerbefragt (wurde der Butler schon befragt), einer Beschreibung für das erste Treffen(z.B. jamesbeschr), sowie seinen Aussagen(z.B. butleraussagen).
				//Zudem existieren zwei Arrays für die Zusammenfassung des Zeugen(z.B. Butler: butlerzsmf und butlerzsmfaussagen). Der erste steht dafür ob die aussagen darüber schon angehört wurden, das zweite Array ist der Text, der dann bei der zusammenfassung gelesen wird.
				
				//butler
					public static boolean butlerbefragt=false;
					public static String jamesbeschr = "The Butler of the house is a grim looking lanky man with pronounced eyebrows and a hooked nose reaching almost down to his lips. He is wearing a worn down, but meticulously cleaned suit.";
					public static String butleraussagen[]=
							//where[0],relation[1],weird[2],susp[3],others[4]
								/*where[0]*/	{"Polishing silver cutlery with the Maid. Why are you asking? Do I seem like a murderer? I have been working here since a long, long time. I’m becoming older, don’t want to risk or lose my job, understand detective?",
								/*relation[1]*/ "Young ‘n stupid man, you know. Had a lot to learn, lately. We Didn’t really have a connection. I was just doing my job here, man.",
								/*weird[2]*/    "A lot of things seem weird these days, don’t  you think? The Old Lady has acted strangely… Bought a lot of stuff. Go ask her and leave me alone.",
								/*susp[3]*/     "Detective, I’m not gonna tell you the secrets of this house. Never have, never will.",
								/*others[4]*/   "I was with the maid. Don’t know anything about the others, mind my own business, you know."};
					//Zusammenfassungen von  butler
					public static boolean[] butlerzsmf= {false,false,false,false,false};
					public static String[] butlerzsmfaussagen= {"The butler claimed that he has been polishing silver when the murder happened. ", "The butler claimed not to have any relationship with the lord. ", 
																"The butler said that the lady was akting strangely. ","The Butler didn't want to tell you whether he had a suspicion about who killed the lord. ","The butler told us that the maid was polishing cutlery with him. "};
				
				//child	
					public static boolean childbefragt=false;
					public static String childbeschr = "A little boy, at the age of 6. He has brown hair and on his shirt there is a red blot, possibly cherry sauce. He is the son of the cook";
					public static String childaussagen[]=
							//where[0],relation[1],weird[2],susp[3],others[4]
							/*where[0]*/	{"I played with my toys! I've got a dinosaur and a giant monkey! And they fought!",
							/*relation[1]*/ "The lord gave me sweets sometimes! I Looooove sweets",
							/*weird[2]*/    "Nope",
							/*susp[3]*/     "The Dinosaur!",
							/*others[4]*/   "My mother cooked, she didn't want to play with me."};
					//zusammenfassung von child
					public static boolean[] childzsmf= {false,false,false,false,false};
					public static String[] childzsmfaussagen = {"The child played with his toys when the murder happened. ","The child said that the lord used to give him sweets. ","The child noticed that the maid spend a lot of her time with Lord Furbish. ","The child suspects that a Dinosaur killed Lord Furbish. ","The child said that his mother, the cook was cooking when the murder happened. "};
					
				//cook	
					public static boolean cookbefragt=false;
					public static String cookbeschr = "The cook is probably the friendliest and most loving person in the whole house. She cares for everyone and makes sure everyone gets enough to eat. She also stands for the mistakes of others. She had a very good relationship with the Lord, although the mother hates this. What do you want to ask";
					public static String cookaussagen[]=
							//where[0],relation[1],weird[2],susp[3],others[4]
							/*where[0]*/	{"Right here Sir. The Lady wanted to have an extravagant supper. I was cooking, baking and preparing all day for it. I’m still not ready Sir.",
							/*relation[1]*/ "Didn’t really have any. I’m in the kitchen or in the basement most of the time. I don’t have enough time for chitchat most of the time. And they treat us as what we are, servants.",
							/*weird[2]*/    "Not that i noticed of. I am focussed on my craft most of the time though.",
							/*susp[3]*/     "Not that i can think of. The Lord payed and treated us well. We are lucky to be here Sir.",
							/*others[4]*/   "I know that my boy was playing in the basement. I’ve heard him pretending to be the prince."};
					// Zusammenfassungen von cook
					public static boolean[] cookzsmf= {false,false,false,false,false};
					public static String[] cookzsmfaussagen = {"The cook claimed to have been cooking while Lord Furbish was killed. ","The cook did not have a relationship with Lord Furbish. ","The cook didn't notice anything weird. ","The cook doesnt suspect anyone of the murder. ","The cook knows that her child was played in the basement during the murder. "};
					
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
					public static boolean[] gardenerzsmf= {false,false,false,false,false};
					public static String[] gardenerzsmfaussagen = {"The gardener said that he was talking to his daughter during the murder. ","The gardner says that he had a decent relationship with the lord. ","The gardener did not notice anything weird. ","The gardener suspects James of the murder. ","The gardener only knows that his daughter the maid was with him. "};
					
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
					public static boolean[] ladyzsmf= {false,false,false,false,false};
					public static String[] ladyzsmfaussagen = {"The Lady was in the garden during the murder. ","The lady had a complicated relationship with her son. ","The lady thinks it's weird that her dresses haven't arrived yet. ","The lady suspects that the servants killed her son. ","The lady noticed that the gardener was not in the garden. "};
					
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
					public static boolean[] maidzsmf= {false,false,false,false,false};
					public static String[] maidzsmfaussagen = {"The maid said that she was polishing silver with the butler. ","The maid says that she was friends with Lord Furbish. ","The maid did not notice anything weird. ","The maid does not suspect anyone of the crime. ","The maid knows that the butler was helping her polish the silver. "};	
				//cat
					public static boolean catbefragt=false;
					public static String catbeschr = "The cat is a white long haired cat with a silk bow on its head. It is throning on the chair in the corner of the room. ";
					public static String cataussagen[]=
							//where[0],relation[1],weird[2],susp[3],others[4]
							/*where[0]*/	{"<audio src=\"soundbank://soundlibrary/animals/amzn_sfx_cat_long_meow_1x_01\"/>",
							/*relation[1]*/ "<audio src=\"soundbank://soundlibrary/animals/amzn_sfx_cat_purr_01\"/>",
							/*weird[2]*/    "<audio src=\"soundbank://soundlibrary/animals/amzn_sfx_cat_purr_02\"/>",
							/*susp[3]*/     "<audio src=\"soundbank://soundlibrary/animals/amzn_sfx_cat_purr_03\"/>",
							/*others[4]*/	"<audio src=\"soundbank://soundlibrary/animals/amzn_sfx_cat_purr_meow_01\"/>"};
					//zusammenfassung von maid 
					public static boolean[] catzsmf= {false,false,false,false,false};
					public static String[] catzsmfaussagen = {"","","","",""};				
		//solve
				public static String readytosolve="You think you solved it? Have you asked enough people? Looked at every clue?";
				public static String sucess="You made it. You found the murderer of Lord Furbish. It was indeed the gardener. Let's see what he has to say.";
				public static String motiv="My poor sweet daughter, the maid, had an affair with Lord Furbish. The Lord tarnished her honor and their relationship was doomed from the start. I took the sword and threatened him so he would leave my daughter in peace. Then I tripped over this damned carpet. The sword pierced his chest and he was dead within seconds.";
				public static Integer versuche=0;
				public static Integer maxversuche=3;
				public static String wrongaccusation= "No, it seems it was not this witness either.";
				public static String escaped="And even worse: I fear that the real murderer has escaped. You were to slow to solve the case. You lost the game.";
				public static String newtry="Do you want another try?";
		//watson
				public static String watsonbegrüßung="How can I help you? I can give you a short summary and I could tell you which rooms you have not yet entered.";
				public static String summary="Ok, here is a short summary.";
				//WoWarIch
					public static String nochbesuchen = "You have not visited the following rooms yet: ";
					public static String schonbesucht = "You have already visited the following rooms:";
					public static String[] roomswwi = {" Entrancehall "," Library "," Garden "," basement "," Servantbedroom "," Kitchen "," Lounge "," Bedchamber "}; //Strings die angefügt werden
					public static boolean räumebesucht []= {entrancehallbesucht,librarybesucht,gardenbesucht,basementbesucht,servantbedroombesucht,kitchenbesucht,loungebesucht,bedchamberbesucht}; //array mit allen boleans

			
			
			
	//RegEX-----------------------------------------------------------------------------------------------------------------
			//universell
				public static String solve= "(i would like to |i want to )?(solve|answer|solution|i know who)(is)?( the mystery|the game|the murder)?";
				public static String watson = "(i would like to |i want to )?(ask )?watson( please)?";
			//zeuge
				public static String where = "where(have you been )?(when the |during the )?(crime was committed |murder was committed |the murder happened |the crime happened )?";
				public static String relationship ="(what was your |did you )?(relationship|like the|hate the|love the)( )?(with)?(lord |the victim )?";
				public static String weird ="(did something seem )?(weird|see)( )?(to you )?(lately |recently )?";
				public static String suspicion="^(do you have any )?(suspicion|idea|murderer|who killed|who commited)( )?(the lord|the crime)?$";
				public static String others="(could you imagine )?other(s)?( committing )?(such a )?(crime)?";
				public static String goodbye="(no|goodbye|farewell|see you|leave)";
				public static String examplequestions="(i would like to hear | i would like to listen )?(the |to )?example question(s)?";
			//räume
				public static String leave ="((i want to |i would like to |i choose the )?(leave|exit|third|last))|(go somewhere else)|(go to another (place|room))";
				public static String search ="(i want to |i would like to | I choose the )?(search|look|second|middle)";
				public static String whitness ="(i want to |i would like to |)?(ask|talk|question|speak|first)( to a| to the)?( whitness| victim| butler| maid| gardener| lady| lord| child| cook)?";
			//solve
				public static String solvebutler ="(it was the |the (murderer|killer) is the )?butler";
				public static String solvechild ="(it was the |the (murderer|killer) is the )?child";
				public static String solvemaid ="(it was the |the (murderer|killer) is the )?maid";
				public static String solvegardener="(it was the |the (murderer|killer) is the )?gardener";
				public static String solvecook="(it was the |the (murderer|killer) is the )?cook";
				public static String solvelady="(it was the |the (murderer|killer) is the )?lady";
				public static String stopsolving="(i want to )?(stop solving )";
				public static String sure="(i am sure|yes, i know who did it)";
			//watson
				public static String askzsmfassung= "(i would like to hear |i would like to listen )?(a |the )?summary( please)?";
				public static String askwwi="(where was i|where have i been|room(s))(( visit(ed)?| go to)| have i (been to|visited)|room(s)?)?( are left| is left)?";
				public static String manual ="(please )?(explain how this works|manual|tutorial)";
				public static String repeatmanual ="(can you |would you )?repeat( the manual| the tutorial| the explaination)";
				public static String yes =" yes|^yes|yes$";
				public static String no=" no|^no|no$";
			
			
	
//FUNKTIONEN___________________________________________________________________________________________________________________________________________			
			
	//Start--------------------------------------------------------------------------------------------------------------------------------------------	
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		logger.info("onSessionStarted:start______________________________________________________________________________________-");
		
		//Nötiges
				userrequest="";	
				text="<speak>"; //dies ist die Variable, die von speak gefüllt und dann durch send() gesendet wird
				murderer="gardener";
				fehleranzahl=0;
				fehler="Es gab folgende Nichterkannte Eingaben:";
				logger.info("onSessionStarted:Nötiges Geladen");
		//Zustand
				aktuellerraum = " ";
				aktuellerzeuge= " ";
				int i;
				for(i=0; i<room.length; i++) {
					room[i]="";
				}
				
				for(i=0; i<zeugenaussagen.length; i++) {
					zeugenaussagen[i]="";
				}
				tutorialzeit=0;
				solvezeit=1;
				watsonzeit=1;
				zeugenzeit=1;
				raumzeit=1;
				
				intutorial=true;
				insolve=false;
				inwatson=false;
				inbefragung=false;
				inraum=false;
				logger.info("onSessionStarted:Zustände Geladen");
		//solve
				versuche=0;
				maxversuche=3;
				logger.info("onSessionStarted:Solve Geladen");
		//räume
			//entryhall
				entrancehallbesucht = false;
			//library	
				librarybesucht = false;
			//garden	
				gardenbesucht = false;
			//servantbedroom	
				servantbedroombesucht = false;
			//kitchen	
				kitchenbesucht = false;
			//basement
				basementbesucht = false;
			//bedchamber	
				bedchamberbesucht = false;
			//lounge	
				loungebesucht = false;
				logger.info("onSessionStarted:Räume Geladen");
		//zeugen	
			//butler
				butlerbefragt=false;
			//child	
				childbefragt=false;
			//cook	
				cookbefragt=false;
			//gardener	
				gardenerbefragt=false;
			//lady	
				ladybefragt=false;				
			//maid	
				maidbefragt=false;
				logger.info("onSessionStarted:Zeugen Geladen");
				
	logger.info("onSessionStarted:end____________________________________________________________________________________");
	}
	
	
	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		logger.info("OnLaunch: start");
		
		//Nötiges
		userrequest="";	
		text="<speak>"; //dies ist die Variable, die von speak gefüllt und dann durch send() gesendet wird
		murderer="gardener";
		fehleranzahl=0; 
		fehler="Es gab folgende Nichterkannte Eingaben:";
		logger.info("onLaunch:Nötiges Geladen");
//Zustand
		aktuellerraum = " ";
		aktuellerzeuge= " ";
		int i;
		for(i=0; i<room.length; i++) {
			room[i]="";
		}
		
		for(i=0; i<zeugenaussagen.length; i++) {
			zeugenaussagen[i]="";
		}
		tutorialzeit=0;
		solvezeit=1;
		watsonzeit=1;
		zeugenzeit=1;
		raumzeit=1;
		
		intutorial=true;
		insolve=false;
		inwatson=false;
		inbefragung=false;
		inraum=false;
		logger.info("onSessionStarted:Zustände Geladen");
//solve
		versuche=0;
		maxversuche=3;
		logger.info("onSessionStarted:Solve Geladen");
//räume
	//entryhall
		entrancehallbesucht = false;
	//library	
		librarybesucht = false;
	//garden	
		gardenbesucht = false;
	//servantbedroom	
		servantbedroombesucht = false;
	//kitchen	
		kitchenbesucht = false;
	//basement
		basementbesucht = false;
	//bedchamber	
		bedchamberbesucht = false;
	//lounge	
		loungebesucht = false;
		logger.info("onSessionStarted:Räume Geladen");
//zeugen	
	//butler
		butlerbefragt=false;
	//child	
		childbefragt=false;
	//cook	
		cookbefragt=false;
	//gardener	
		gardenerbefragt=false;
	//lady	
		ladybefragt=false;				
	//maid	
		maidbefragt=false;
		logger.info("onSessionStarted:Zeugen Geladen");
	
		
		tutorial(0);
		logger.info("OnLaunch: ende");
		return sende();
	
		}

	//onIntent() bekommt eine kryptische Nachricht von Alexa und entscheidet daraufhin welche funktion es zum weitermachen nutzen soll
	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope){
		logger.info("___________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________");
		IntentRequest request = requestEnvelope.getRequest();
		
		Intent intent = request.getIntent();
		logger.info("onIntent: ");
		ArrayList<String> slots = new ArrayList<String>();
		slots.addAll(intent.getSlots().keySet());
		for (int i = 0; i<slots.size(); i++) {
			String slotname = slots.get(i);
			logger.info(slotname+": "+intent.getSlot(slotname).getValue());
		}
		userrequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userrequest + "], on Intent");
		if (intutorial) {//Abfrage der einzelnen Ebenen des Programms
			tutorial(tutorialzeit);
		}
		else if (insolve) {
			solve(solvezeit);				
		}
		else if (inwatson) {
				watson(watsonzeit);				
		}
		else if (inbefragung) {
			zeuge(aktuellerzeuge, zeugenzeit);
		} 
		else if (inraum) {
			raum(aktuellerraum, raumzeit);
		}
		else {
			logger.info("onIntent(): Probleme den richtigen Zustand auszuwählen." +intutorial +" "+inbefragung+" "+inraum+" "+insolve+" "+inwatson);
		}
		return sende();}


	//Logik-----------------------------------------------------------------------------------------------------------------------------------------
		
		//not() dreht den Wahrheitswert um
			public static boolean not(boolean bool) {
				if (bool) {
					return false;}
				else {
					return true;
				}
			}
	
	//Ausgabe---------------------------------------------------------------------------------------------------------------------------------------
		//sende() sorgt dafür, dass Alexa die variable text ausspricht
			public static SpeechletResponse sende() {
				logger.info("sende(): start");
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
				logger.info("sende(): ende");
				logger.info("");
				logger.info("");
				logger.info("");
				return SpeechletResponse.newAskResponse(speech, rep);
			};
			
		//speak	verändert den String text, welcher von sende() zu einer ssml verarbeitet und versendet wird. So können cerschiedene Leute hintereinander sprechen
			public static void speak(String sentence, String name) {
				logger.info("speak("+sentence+","+name+"): start");
				logger.info(name+": "+sentence);
				//logger.info("speak("+sentence+","+name+"): wähle Person");
				switch(name){ 
				case "narrator": 
					//logger.info("speak("+sentence+","+name+"): narrator");
					//logger.info("Wir sind jetzt im Fall Narrator" + text);
					text=text+"<voice name=\"Matthew\"> "+ sentence + "</voice>";
					break;
				case "watson": 
					//logger.info("Wir sind jetzt im Fall Watson" + text);
					//logger.info("speak("+sentence+","+name+"): watson");
					text=text+"<voice name=\"Brian\"> "+ sentence + "</voice>";
					break; 
				case "maid": 
					//logger.info("speak("+sentence+","+name+"): maid");
					text=text+"<voice name=\"Kimberly\">"+ sentence + "</voice>";
					break; 
				case "gardener": 
					logger.info("speak("+sentence+","+name+"): gardener");
					text=text+"<voice name=\"Russell\">" + sentence + "</voice>";
					logger.info("speak("+sentence+","+name+"): gardener, text=" + text + " 					sentence=" + sentence );
					break; 
				case "butler": 
					//logger.info("speak("+sentence+","+name+"): butler");
					text=text+"<voice name=\"Joey\">" + sentence + "</voice>";
					break;
				case "cook": 
					//logger.info("speak("+sentence+","+name+"): cook");
					text=text+"<voice name=\"Kendra\">" + sentence + "</voice>";
					break;
				case "child": 
					//logger.info("speak("+sentence+","+name+"): child");
					text=text+"<voice name=\"Justin\">" + sentence + "</voice>";
					break;
				case "lady": 
					//logger.info("speak("+sentence+","+name+"): wähle lady");
					text=text+"<voice name=\"Amy\">" + sentence + "</voice>";
					break;
				case "cat": 
					//logger.info("speak("+sentence+","+name+"): wähle lady");
					text=text+"<voice name=\"Amy\">" + sentence + "</voice>";
					break;
				default:
					//logger.info("speak("+sentence+","+name+"): default");
					text=text+"<voice name=\"Matthew\"> "+ sentence + "</voice>";
					//logger.info("text enthält jetzt: "+text);
				}
				logger.info("speak("+sentence+","+name+"): ende");

				return;
			}
	
		//sound() bekommt entweder den link zu einer entsprechenden MP3 oder einen schon im Programm vorhandenen Sound
			public static void sound(String link) {
				if (link=="rain"){
						link="soundbank://soundlibrary/weather/rain/rain_07";
				}
				else if(link=="cat"){
					Random r = new Random();
					int z=r.nextInt(5);
					switch(z) {
					case 1:
						link="soundbank://soundlibrary/animals/amzn_sfx_cat_long_meow_1x_01";
						break;
					case 2:
						link="soundbank://soundlibrary/animals/amzn_sfx_cat_purr_01";
						break;
					case 3:
						link="soundbank://soundlibrary/animals/amzn_sfx_cat_purr_02";
						break;
					case 4:
						link="soundbank://soundlibrary/animals/amzn_sfx_cat_purr_meow_01";
						break;}
				}else {
					logger.info("Der Sound-Link [\""+link+"\"] funktioniert leider nicht.");
				}
				text+="<audio src=\""+link+"\"/>";
				return;};
				
		//pardon() ist eine funktion, falls etwas nicht verstanden wurde
			public static void pardon(String name, String ort) { //befragung,
				logger.info("pardon("+name+","+ort+"): start");
				fehleranzahl=fehleranzahl+1;
				fehler=fehler+userrequest+" und ";
				Random r = new Random();
				int z=r.nextInt(3);
				logger.info("pardon("+name+","+ort+"): randomzahl="+z);
				switch(ort) {
					case "zeuge":
						logger.info("pardon("+name+","+ort+"): hat erkannt, dass wir in einer Zeugenbefragung sind");
						if (name!="cat") {
						switch(z) {
						case 0: 
							speak("Could you repeat your question?", name);
							break;
						case 1:
							speak("I did not understand your question", name);
							break;
						case 2:
							speak("I think, that your witness did not understand your question", "watson");
							break;
						}
						}
						else {
							sound("cat");
						}
						break;
					case "raum":
						logger.info("pardon("+name+","+ort+"): hat erkannt, dass wir in einem raum sind");
						switch(z) {
						case 0: 
							logger.info("pardon("+name+","+ort+"): raum, spruch1");
							speak("Could you repeat your answer?", "watson");
							break;
						case 1:
							logger.info("pardon("+name+","+ort+"): raum, spruch2");
							speak("What were you saying?", "watson");
							break;
						case 2:
							logger.info("pardon("+name+","+ort+"): raum, spruch3");
							speak("I beg your pardon", "watson");
							break;
						}
						break;
					case "watson":
						logger.info("pardon("+name+","+ort+"): hat erkannt, dass wir in einem watson sind");
						switch(z) {
						case 0: 
							speak("Could you repeat your answer?", "watson");
							break;
						case 1:
							speak("What were you saying?", "watson");
							break;
						case 2:
							speak("I beg your pardon", "watson");
							break;
						}
						break;
				}
				logger.info("pardon("+name+","+ort+"): ende");
				return;}	
	
	//tutorial() sorgt für das tutorial
		public static void tutorial(int zeit) {
			logger.info("tutorial("+zeit+"): start");
			logger.info("tutorial("+zeit+"): wähle Zeit");
			switch(zeit) {
			case 0:
				sound("rain");
				speak(einleitung1, "narrator");
				speak(tutorialfrage, "watson");
				tutorialzeit=1;
				
				//speak(sucess, "watson");
				//speak(motiv,murderer);
				
				
				break;
			case 1://Willst du ein tutorial?
				logger.info("tutorial("+zeit+"): Zeit=1");
				if(not(yesno(userrequest))) {//tutorial
					intutorial=true;
					tutorialzeit=2;
					speak(tutorial1,"watson");
				}
				else {//keintutorial
					inraum=true;
					intutorial=false;
					speak(einleitung2,"narrator");
					aktuellerraum="entrancehall";
					raumzeit=0;
					raum("entrancehall",0);
					entrancehallbesucht=true;
					
				}
				break;
			case 2:						//Verstanden?
				logger.info("tutorial("+zeit+"): Zeit=2");
				if(yesno(userrequest)) {			//tutorial1 verstanden 
					intutorial=true;
					tutorialzeit=3;
					speak(tutorial2,"watson");
				}
				else {								//tutorial1 nicht verstanden
					speak(tutorial1,"watson");
				}	
				break;
			case 3:						//Verstanden?
				logger.info("tutorial("+zeit+"): Zeit=3");
				if(yesno(userrequest)) {			//tutorial2 nicht verstanden 
					intutorial=true;
					speak(tutorial2,"watson");
				}
				else {								//tutorial2 verstanden 
					tutorialzeit=4;
					aktuellerraum="entrancehall";
					speak(tutorial3,"watson");
				}
			break;
			case 4:						//Verstanden?
				logger.info("tutorial("+zeit+"): Zeit=4");
				if(yesno(userrequest)) {	//tutorial3 verstanden 
					inraum=true;
					intutorial=false;
					speak(einleitung2,"narrator");
					aktuellerraum="entrancehall";
					raumzeit=1;
					speak(entrancehall[0], "narrator");
					speak(entrancehall[1], "narrator");
					entrancehallbesucht=true;
				}
				else {						//tutorial3 nicht verstanden 
					tutorialzeit=4;
					aktuellerraum="entrancehall";
					speak(tutorial3,"watson");
					
				}
				break;
			}
			
			return;}


	//Zustände-----------------------------------------------------------------------------------------------------------------------------------------
		//solve() ist die funktion zum auflösen des Falles
			public static void solve(int zeit) {
				logger.info("solve("+zeit+"):"+"gestartet");
				logger.info("solve("+zeit+"):"+"wähle Zeit");
				switch(zeit) {
				case 0://bist du bereit zu solven?
					logger.info("Der Spieler möchte Solven");
					insolve=true;
					speak(readytosolve,"watson");
					solvezeit=1;
					break;
				case 1://Wer war es?/Leave
					logger.info("solve("+zeit+"):"+"Zeit=1");
					Boolean yn= yesno(userrequest);
					if(yn) {
						speak("I am impressed. Who do you think is the murderer?", "watson");
						solvezeit=2;
					}
					else {
						speak("Maybe we should head back to our case then","watson");
						insolve=false;
						if (inwatson==true){watson(0);logger.info("solve("+zeit+"):"+"Es wurde richtung watson verlassen");}
						else if(inbefragung==true){logger.info("solve("+zeit+"):"+"Es wurde richtung befragung verlassen"); zeuge(aktuellerzeuge,0);}
						else {raum(aktuellerraum,0); logger.info("solve("+zeit+"):"+"Es wurde richtung raum verlassen");}
					}
					break;
						
				case 2://Richtig oder Falsch
					logger.info("solve("+zeit+"):"+"Zeit=2");
					String verdacht=who(userrequest);
					logger.info(verdacht+" wurde verdächtigt der Täter gewesen zu sein");
					if (verdacht==murderer){//richtiger verdacht
						speak(sucess, "watson");
						speak(motiv, murderer);
						
						//TODO Programm beenden
						break;
					}
					else {//Falscher verdacht
						speak(wrongaccusation, "watson");
						versuche=versuche+1;
						if (versuche<maxversuche) {
							speak(newtry, "watson");
							solvezeit=1;
						}
						else {
							speak(escaped, "watson");
							
							//TODO Programm beenden
						}
					}
					break;
				}
				
				return;
			}
	
		//Die Funktion, die Watson zum leben erweckt
			public static void watson(int zeit) {
				logger.info("watson("+zeit+"):"+"start");
				logger.info("watson("+zeit+"):"+"wähle Zeit");
				switch(zeit) {
					case 0://Begrüßung
						inwatson=true;
						logger.info("watson("+zeit+"):"+"Zeit=1");
						speak(watsonbegrüßung,"watson");
						watsonzeit=1;
						break;
					case 1://frageauswahl
						logger.info("watson("+zeit+"):"+"Zeit=2");
						int Frage=RecUI(userrequest,7);
						switch(Frage) {
							case 1://Zusammenfassung
								logger.info("watson("+zeit+"):"+"Frage=1");
								logger.info("Watson wurde um zusammenfassung gebeten");
								speak(zusammenfassung(), "watson");
								speak("How else can I help you?", "watson");
								break;
							case 2://where was I?
								logger.info("watson("+zeit+"):"+"Frage=2");
								logger.info("Watson wurde gefragt wo wir waren");
								speak(wwi(),"watson");
								speak("How else can I help you?", "watson");
								watsonzeit=1;
								break;
							case 3://solve
								logger.info("watson("+zeit+"):"+"Frage=3");
								solve(0);
								break;
							case 4://leave
								logger.info("watson("+zeit+"):"+"Frage=4");
								logger.info("Watson wurde verlassen");
								inwatson=false;
								watsonzeit=0;
								if(inbefragung==true){logger.info("watson("+zeit+"):"+"Es wurde richtung befragung verlassen"); zeuge(aktuellerzeuge,0);}
								else {raum(aktuellerraum,0);logger.info("watson("+zeit+"):"+"Es wurde richtung befragung verlassen");}
								break;
							case 1400://None
								logger.info("watson("+zeit+"):"+"Zeit=1400");
								pardon("watson","watson");
								watsonzeit=1;
								break;
						}
					break;
				}	
			return;
			}
		
		
		//zeuge - Funktion, wenn man mit jemandem redet
			public static void zeuge(String name, int zeit) {
				logger.info("zeuge("+name+","+zeit+"): start");
				inbefragung=true;
				
				
				logger.info("zeuge("+name+","+zeit+"):"+"Wähle Zeitpunkt");
				switch(zeit){
				case 0:
					inbefragung=true;
					zeugenzeit=1;
					switch (aktuellerzeuge) {//welcher zeuge wird befragt 
						case "butler": 
							if (butlerbefragt==false) {
							butlerbefragt=true;
							speak(jamesbeschr,"narrator");
							}
							zeugenaussagen=butleraussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
						case "maid":
							if (maidbefragt==false) {
							maidbefragt=true;
							speak(maidbeschr,"narrator");
							}
							zeugenaussagen=maidaussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
						case "cook":
							if (cookbefragt==false) {
							cookbefragt=true;
							speak(cookbeschr,"narrator");
							}
							zeugenaussagen=cookaussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
						case "gardener":
							if (gardenerbefragt==false) {
							gardenerbefragt=true;
							speak(gardenerbeschr,"narrator");
							}
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							zeugenaussagen=gardeneraussagen;
							break;
						case "lady":
							if (ladybefragt==false) {
							ladybefragt=true;
							speak(ladybeschr,"narrator");
							}
							zeugenaussagen=ladyaussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
						case "child":
							if (childbefragt==false) {
								childbefragt=true;
								speak(childbeschr,"narrator");
							}
							zeugenaussagen=childaussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
						case "cat":
							if (catbefragt==false) {
							catbefragt=true;
							speak(catbeschr,"narrator");
							}
							zeugenaussagen=cataussagen;
							speak(frageaufruf,"watson"); //fragt mit Watsons Stimme, was getan werden soll
							break;
					}
					zeugenzeit=1;
					break;
				case 1://Zeuge wurde beschrieben und es wurde gerade gefragt, welche Frage gestellt werden soll
					int FRAGE=RecUI(userrequest, 2);
					logger.info("zeuge("+name+","+zeit+"):"+"Wähle Frage");
					switch (FRAGE) {
						case 1://examplequestions
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=1");
							speak(exampleq, "watson");
							speak("Do you want to ask anything else?","watson");
							break;
						case 2:// where
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=2");
							speak(zeugenaussagen[0],name);
							switch(name) {
							case "butler":
								butlerzsmf[0]=true;break;
							case "child":
								childzsmf[0]=true;break;
							case "cook":
								cookzsmf[0]=true;break;
							case "gardener":
								gardenerzsmf[0]=true;break;
							case "lady":
								ladyzsmf[0]=true;break;
							case "maid":
								maidzsmf[0]=true;break;
							case "cat":
								catzsmf[0]=true;break;
							}
							speak("Do you want to ask anything else?","watson");
							break;
						case 3://relation
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=3");
							speak(zeugenaussagen[1],name);
							switch(name) {
							case "butler":
								butlerzsmf[1]=true;break;
							case "child":
								childzsmf[1]=true;break;
							case "cook":
								cookzsmf[1]=true;break;
							case "gardener":
								gardenerzsmf[1]=true;break;
							case "lady":
								ladyzsmf[1]=true;break;
							case "maid":
								maidzsmf[1]=true;break;
							case "cat":
								catzsmf[1]=true;break;
							}
							speak("Do you want to ask anything else?","watson");
							break;
						case 4://weird
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=4");
							speak(zeugenaussagen[2],name);
							switch(name) {
							case "butler":
								butlerzsmf[2]=true;
								break;
							case "child":
								childzsmf[2]=true;
								break;
							case "cook":
								cookzsmf[2]=true;
								break;
							case "gardener":
								gardenerzsmf[2]=true;
								break;
							case "lady":
								ladyzsmf[2]=true;
								break;
							case "maid":
								maidzsmf[2]=true;
								break;
							case "cat":
								catzsmf[2]=true;break;	
							}
		
							speak("Do you want to ask anything else?","watson");
							break;
						case 5://suspicion
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=5");
							speak(zeugenaussagen[3],name);
							switch(name) {
							case "butler":
								butlerzsmf[3]=true;
								break;
							case "child":
								childzsmf[3]=true;
								break;
							case "cook":
								cookzsmf[3]=true;
								break;
							case "gardener":
								gardenerzsmf[3]=true;
								break;
							case "lady":
								ladyzsmf[3]=true;
								break;
							case "maid":
								maidzsmf[3]=true;
								break;
							case "cat":
								catzsmf[3]=true;break;	
							}
							speak("Do you want to ask anything else?","watson");
							break;
						case 6://others
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=6");
							speak(zeugenaussagen[4],name);
							switch(name) {
							case "butler":
								butlerzsmf[4]=true;
								break;
							case "child":
								childzsmf[4]=true;
								break;
							case "cook":
								cookzsmf[4]=true;
								break;
							case "gardener":
								gardenerzsmf[4]=true;
								break;
							case "lady":
								ladyzsmf[4]=true;
								break;
							case "maid":
								maidzsmf[4]=true;
								break;
							case "cat":
								catzsmf[4]=true;break;
							}
		
							speak("Do you want to ask anything else?","watson");
							break;
						case 7:// abschied
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=7");
							inbefragung=false;
							inraum=true;
							raumzeit=1;
							zeugenzeit=1;
							speak("Maybe we can find something else in this room","watson");
							raum(aktuellerraum,0);
							return;
						case 8://solve
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=8");
							logger.info("Der Spieler möchte Solven");
							solve(0);
							insolve=true;
							
							break;
						case 9://watson
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=9");
							inwatson=true;
							watsonzeit=0;
							watson(0);
							break;
						case 1400:
							logger.info("zeuge("+name+","+zeit+"):"+"Frage=1400");
							pardon(aktuellerzeuge,"zeuge");
							break;
					}
					break;
					
				}
				
			return;}
			
			
		//raum() ist eine Funktion mit der man Räume besuchen kann.		
			public static void raum(String raum, int zeit) {
				logger.info("raum("+raum+","+zeit+"):"+"startet");
				logger.info("raum("+raum+","+zeit+"):"+"wähle Zeit");
				switch(zeit) {
				case 0:
					inraum=true;
					aktuellerraum=raum;
					//stellt die richtigen Raumdaten zur verfügen, beschreibt räume, falls nötig
					logger.info("raum("+raum+","+zeit+"):"+"wählt Raum");
					switch (raum) {
							case "library":
								logger.info("raum("+raum+","+zeit+"):"+"library");
								room=library;
								if (librarybesucht==false) {
									speak(library[0],"narrator");
									librarybesucht=true;
									logger.info(raum + " wurde beschrieben");
									}
								break;
								
							case "lounge":	
								logger.info("raum("+raum+","+zeit+"):"+"lounge");
								room=lounge;
								if (loungebesucht==false) {
									speak(lounge[0],"narrator");
									loungebesucht=true;
									logger.info(raum + " wurde beschrieben");
								}
								break;
								
							case "garden":	
								logger.info("raum("+raum+","+zeit+"):"+"garden");
								room=garden;
								if (gardenbesucht==false) {
									speak(room[0],"narrator");
									gardenbesucht=true;
									logger.info(raum + " wurde beschrieben");
									}	
								break;
								
							case "basement":	
								logger.info("raum("+raum+","+zeit+"):"+"basement");
								room=basement;
								if (basementbesucht==false) {
									speak(room[0],"narrator");
									basementbesucht=true;
									logger.info(raum + " wurde beschrieben");
									}	
								break;
				
							case "servantbedroom":	
								room=servantbedroom;
								logger.info("raum("+raum+","+zeit+"):"+"servantbedroom");
								if (servantbedroombesucht==false) {
									speak(room[0],"narrator");
									servantbedroombesucht=true;
									logger.info(raum + " wurde beschrieben");
									}	
								break;
				
							case "bedchamber":	
								logger.info("raum("+raum+","+zeit+"):"+"bedchamber");
								room=bedchamber;
								if (bedchamberbesucht==false) {
									speak(room[0],"narrator");
									bedchamberbesucht=true;
									logger.info(raum + " wurde beschrieben");
									}
								break;
				
							case "kitchen":	
								logger.info("raum("+raum+","+zeit+"):"+"kitchen");
								room=kitchen;
								if (kitchenbesucht==false) {
									speak(room[0],"narrator");
									kitchenbesucht=true;
									logger.info(raum + " wurde beschrieben");
									}	
								break;
				
							case "entrancehall":	
								logger.info("raum("+raum+","+zeit+"):"+"entrancehall");
								room=entrancehall;
								if (entrancehallbesucht==false) {
									speak(room[0],"narrator");
									entrancehallbesucht=true;
									logger.info(raum + " wurde beschrieben");
									}
								break;
								
							}
					speak(room[1], "watson");//erneute beschreibung der Tätigkeiten im Raum
					raumzeit=1;
					break;
					
				case 1:
					logger.info("raum("+raum+","+zeit+"):"+"Zeit=1");
					int wastun=RecUI(userrequest,1);
					logger.info("raum("+raum+","+zeit+"):"+"wähle aktion");
					switch (wastun) {
						case 1://watson
							logger.info("raum("+raum+","+zeit+"):"+"wastun=1");
							watson(0);
							raumzeit=1;
							break;
						case 2://search room
							logger.info("raum("+raum+","+zeit+"):"+"wastun=2");
							logger.info(raum + " wurde genauer untersucht");
							speak(room[2], "narrator");
							speak(room[1], "narrator");
							break;
						case 3://zeuge
							logger.info("raum("+raum+","+zeit+"):"+"wastun=3");
							aktuellerzeuge=room[4];
							//logger.info("raum("+raum+","+zeit+"):"+"aktuellerzeuge geladen");
							
							zeuge(aktuellerzeuge,0);
							break;
						case 4://leave
							logger.info("raum("+raum+","+zeit+"):"+"wastun=4");
							logger.info(raum + " wurde verlassen");
							speak(room[3],"watson");
							raumzeit=2;
							//TO DO: Extra case für das verlassen des raumes anlegen
							break;
						case 5:
							logger.info("raum("+raum+","+zeit+"):"+"wastun=5");
							solvezeit=1;
							insolve=true;
							break;
						case 1400:
							logger.info("raum("+raum+","+zeit+"):"+"wastun=1400");
							pardon("watson", "raum");
						}
					break;
				case 2://raum verlassen
					logger.info("raum("+raum+","+zeit+"):"+"Zeit=2");
					int wunschort=RecUI(userrequest,4);
					if  (wunschort== 1400) {
						speak("Sorry, I did not understand you"+room[3],"watson");
					}
					else {
						aktuellerraum=raumarray[wunschort];
						speak("you enter the "+aktuellerraum, "narrator");
						raum(aktuellerraum,0);
						
					}//else im raum verlassen
					break;
				}//raum verlassen
				
				return;}
	
			
	//Willenserkennung---------------------------------------------------------------------------------------------------------------------------------
		//RecUI erkennt die Benutzeräußerung
			public static int RecUI(String UserRequest, int ort) { //1=Raum, 2=Zeuge, 3=Solve, 4=Leave, 5=yesno, 6=tutorial 7=watson 8=who
				logger.info("RecUI("+UserRequest+","+ort+"):start");
				UserRequest = UserRequest.toLowerCase().trim();
				switch(ort) {
				case 1: //hier kommen all die Dinge rein, die erkannt werden sollen, wenn man im Raum etwas tut			
					//Benennung der Patterns: z.B. p15=Case 1, Pattern5
					Pattern p11 = Pattern.compile(watson);
					Pattern p12 = Pattern.compile(search);
					Pattern p13 = Pattern.compile(whitness);
					Pattern p14 = Pattern.compile(leave);
					Pattern p15 = Pattern.compile(solve);
					Matcher m11 = p11.matcher(UserRequest);
					Matcher m13 = p13.matcher(UserRequest);
					Matcher m12 = p12.matcher(UserRequest);
					Matcher m14 = p14.matcher(UserRequest);
					Matcher m15 = p15.matcher(UserRequest);
					if (m11.find()) {
						logger.info("Watson wird konsultiert.");
						return 1;} 
					else if (m12.find()) {
						logger.info("Der Raum wird durchsucht.");
						return 2;} 
					else if (m13.find()) {
						logger.info("Der Zeuge wird befragt.");
						return 3;} 
					else if (m14.find()) {
						logger.info("Der Spieler verlässt den raum");
						return 4;} 
					else if (m15.find()) {
						logger.info("Der Spieler möchte Solven");
						solvezeit=1;
						insolve=true;
						speak(readytosolve,"watson");
						return 5;}
					else {
						logger.info("RecUI:case 1: Kein RecState wurde erkannt: "+UserRequest);
						return 1400;
					}
				case 2: //hier kommen all die Dinge rein, die erkannt werden sollen, wenn man einen Zeugen befragt
						//Benennung der Patterns: z.B. p15=Case 1, Pattern5
						//String a ="^a$";
						Pattern p21 = Pattern.compile(examplequestions);
						Pattern p22 = Pattern.compile(where);
						Pattern p23 = Pattern.compile(relationship);
						Pattern p24 = Pattern.compile(weird);
						Pattern p25 = Pattern.compile(suspicion);
						//Pattern p25 = Pattern.compile(a);
						Pattern p26 = Pattern.compile(others);
						Pattern p27 = Pattern.compile(goodbye);
						Pattern p28 = Pattern.compile(solve);
						Pattern p29 = Pattern.compile(watson);
	
						Matcher m21 = p21.matcher(UserRequest);
						Matcher m22 = p22.matcher(UserRequest);
						Matcher m23 = p23.matcher(UserRequest);
						Matcher m24 = p24.matcher(UserRequest);
						Matcher m25 = p25.matcher(UserRequest);
						Matcher m26 = p26.matcher(UserRequest);
						Matcher m27 = p27.matcher(UserRequest);
						Matcher m28 = p28.matcher(UserRequest);
						Matcher m29 = p29.matcher(UserRequest);
						
						if (m21.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach Beispielfragen gefragt");
							return 1;} 
						else if (m22.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach dem Aufenthaltsort gefragt");
							return 2;} 
						else if (m23.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach Beziehungen gefragt");
							return 3;} 
						else if (m24.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach Merkwürdigem gefragt");
							return 4;} 
						else if (m25.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach Verdächtigungen gefragt");
							return 5;}
						else if (m26.find()) {
							logger.info("Bei der Zeugenbefragung wurde nach den Anderen gefragt");
							return 6;}
						else if (m27.find()) {
							logger.info("Die Zeugenbefragung mit "+ aktuellerzeuge + " wurde beendet--------------------------------------");
							return 7;}
						else if (m28.find()) {
							logger.info("Solve vorgang erbeten");
							return 8;}
						else if (m29.find()) {
							logger.info("Bei der Zeugenbefragung wird Watson konsultiert");
							return 9;}
						else { 
							logger.info("RecUI:case 2: Kein RecState wurde erkannt: "+UserRequest);
							return 1400;}
				case 3:
					//hier kommen all die Dinge rein, die erkannt werden sollen, wenn man solven möchte
					//Benennung der Patterns: z.B. p15=Case 1, Pattern5
					Pattern p31 = Pattern.compile(solvebutler);
					Pattern p32 = Pattern.compile(solvechild);
					Pattern p33 = Pattern.compile(solvemaid);
					Pattern p34 = Pattern.compile(solvegardener);
					Pattern p35 = Pattern.compile(solvecook);
					Pattern p36 = Pattern.compile(solvelady);
					Pattern p37 = Pattern.compile(stopsolving);
					Pattern p38 = Pattern.compile(sure);
	
					Matcher m31 = p31.matcher(UserRequest);
					Matcher m32 = p32.matcher(UserRequest);
					Matcher m33 = p33.matcher(UserRequest);
					Matcher m34 = p34.matcher(UserRequest);
					Matcher m35 = p35.matcher(UserRequest);
					Matcher m36 = p36.matcher(UserRequest);
					Matcher m37 = p37.matcher(UserRequest);
					Matcher m38 = p38.matcher(UserRequest);
					
					if (m31.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 1;} 
					else if (m32.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 2;} 
					else if (m33.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 3;} 
					else if (m34.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 4;} 
					else if (m35.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 5;}
					else if (m36.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 6;}
					else if (m37.find()) {
						logger.info("Der User bricht solving ab");
						return 7;}
					else if (m38.find()) {
						logger.info("Der User ist sich sicher");
						return 8;}
					else { 
						logger.info("RecUI:case 3: Kein RecState wurde erkannt: "+UserRequest);
						return 1400;}
					
				case 4://leave
					int i;
					for(i=0;i<raumregex.length;i++){
						Pattern p4 = Pattern.compile(raumregex[i]);
						Matcher m4 = p4.matcher(UserRequest);
						if (m4.find()) {
							logger.info("Der User möchte in diesen Raum gehen: "+raumregex[i]);
							return i;
						}
					}
					logger.info("RecUI:case 4: Kein RecState wurde erkannt: "+UserRequest);
					return 1400;
					
				case 5:// Yesno
					Pattern p51 = Pattern.compile(yes);
					Pattern p52 = Pattern.compile(no);
					
					Matcher m51 = p51.matcher(UserRequest);
					Matcher m52 = p52.matcher(UserRequest);
					
					if (m51.find()) {
						logger.info("Es wurde Yes erkannt");
						return 1;}
					else if (m52.find()) {
						logger.info("Es wurde No erkannt");
						return 2;}
					else { 
						logger.info("RecUI:case 5: Kein RecState wurde erkannt: "+UserRequest);
					return 1400;}
					
				case 6://tutorial-muss noch überarbeitet werden
					Pattern p61 = Pattern.compile(yes);
					
					Matcher m61 = p61.matcher(UserRequest);
					
					if (m61.find()) {
						logger.info("Der User möchte bricht solving ab");
						return 1;}
					else { 
						logger.info("RecUI:case 5: Kein RecState wurde erkannt: "+UserRequest);                             
					return 1400;}
					
				case 7: //hier kommen all die Dinge rein, die erkannt werden sollen, wenn man mit Watson etwas tut			
					//Benennung der Patterns: z.B. p15=Case 1, Pattern5
					Pattern p71 = Pattern.compile(askzsmfassung);//zusammenfassung
					Pattern p72 = Pattern.compile(askwwi);//where was I
					Pattern p73 = Pattern.compile(solve);//solve
					Pattern p74 = Pattern.compile(leave);//leave
					
					Matcher m71 = p71.matcher(UserRequest);
					Matcher m73 = p73.matcher(UserRequest);
					Matcher m72 = p72.matcher(UserRequest);
					Matcher m74 = p74.matcher(UserRequest);
					
					if (m71.find()) {
						logger.info("Der User will eine zusammenfassung");
						return 1;} 
					else if (m72.find()) {
						logger.info("Der User will wissen, wo er war");
						return 2;} 
					else if (m73.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 3;} 
					else if (m74.find()) {
						logger.info("Der User vermutet, dass es der Butler war");
						return 4;} 
					else {
						return 1400;
					}
	
				}
				logger.info("FATAL ERROR-RecUI:Kein Case wurde erkannt:" + ort);  
				return ort;
				
			}
	
		//yesno(): wie (UserRequest, 5), returnt allerdings einen bool
			public static boolean yesno(String UserRequest) {
				int z=RecUI(UserRequest, 5);
				if (z==1) {return true;}
				else {
					return false;} 
				}
	
		//who() erkennt die namen und berufe der Zeugen
			public static String who(String userrequest) {
				logger.info("who("+userrequest+"):"+"start");
				String name=new String();
				String Zeuge1="(butler|james)";
				String Zeuge2="(gardener)";
				String Zeuge3="(maid)";
				String Zeuge4="(lady)";
				String Zeuge5="(cook)";
				String Zeuge6="(child)";
				logger.info("who("+userrequest+"):"+"strings geladen");
				Pattern p1 = Pattern.compile(Zeuge1);
				Matcher m1 = p1.matcher(userrequest);
				Pattern p2 = Pattern.compile(Zeuge2);
				Matcher m2 = p2.matcher(userrequest);
				Pattern p3 = Pattern.compile(Zeuge3);
				Matcher m3 = p3.matcher(userrequest);
				Pattern p4 = Pattern.compile(Zeuge4);
				Matcher m4 = p4.matcher(userrequest);
				Pattern p5 = Pattern.compile(Zeuge5);
				Matcher m5 = p5.matcher(userrequest);
				Pattern p6 = Pattern.compile(Zeuge6);
				Matcher m6 = p6.matcher(userrequest);
				logger.info("who("+userrequest+"):"+"patterns compiled und gematcht");
				if (m1.find()) {
					return "butler";} 
				if (m2.find()) {
					return "gardener";} 
				if (m3.find()) {
					return "maid";} 
				if (m4.find()) {
					return "lady";} 
				if (m5.find()) {
					return "cook";} 
				if (m6.find()) {
					return "child";} 
				logger.info("who("+userrequest+"):"+"end: "+name);
				return name;
			}

	//Wissensbasis---------------------------------------------------------------------------------------------------------------------------------
		//wwi() weiß wo wir schon waren und wo nicht
			public static String wwi() {//Funktion um zu erkennen in welchem Raum ich war
				logger.info("wwi():"+"start");
				räumebesucht [0]= entrancehallbesucht;
				räumebesucht [1]= librarybesucht;
				räumebesucht [2]= gardenbesucht;
				räumebesucht [3]= basementbesucht;
				räumebesucht [4]= servantbedroombesucht;
				räumebesucht [5]= kitchenbesucht;
				räumebesucht [6]= loungebesucht;
				räumebesucht [7]= bedchamberbesucht; //array mit allen boleans
				
				int i;
				for (i = 0; i < räumebesucht.length; i++) { // durch alle booleans durchschalten
					logger.info("wwi():"+"raum="+i);
					if (räumebesucht[i]==false) { //wenn man noch nicht in x war dann..
						logger.info("wwi():"+"räumebesucht[i]==false");
						nochbesuchen = nochbesuchen+roomswwi[i]+", ";}
					else if (räumebesucht[i]==true) { //wenn man schon in y war dann..
						logger.info("wwi():"+"räumebesucht[i]==true");
						schonbesucht = schonbesucht+roomswwi[i]+", ";						
					}
				}
				logger.info("wwi():"+"ende:"+schonbesucht+"."+nochbesuchen);

				String a=schonbesucht+nochbesuchen;
			return a;  // Gesamte Ausgabe 
			}
	
		
	
		//zsmfpers() gibt die zusammenfassung, was eine einzelne person sagte
			public static String zsmfpers(boolean[] gefragt, String[] gesagt) {
				logger.info("zsmfpers("+gefragt+", "+gesagt+"):"+"start");
				String zsmf="";//TODO Zusammenfassung debuggen
				for(Integer i=0;i<gefragt.length;i++) {
					if(gefragt[i]) {
						zsmf=zsmf+gesagt[i];
					}
				}	
				logger.info("zsmfpers("+gefragt+", "+gesagt+"):"+"ende:"+zsmf);
				return zsmf;
			}
		//gibt eine gesammtzusammenfassung	
			public static String zusammenfassung() {
				logger.info("zusammenfassung():"+"start");
				String zsmf="";
				if (butlerbefragt) {
					zsmf=zsmf+zsmfpers(butlerzsmf, butlerzsmfaussagen);}
				if (childbefragt) {
					zsmf=zsmf+zsmfpers(childzsmf, childzsmfaussagen);}
				if (cookbefragt) {
					zsmf=zsmf+zsmfpers(cookzsmf, cookzsmfaussagen);}
				if (gardenerbefragt) {
					zsmf=zsmf+zsmfpers(gardenerzsmf, gardenerzsmfaussagen);}
				if (ladybefragt) {
					zsmf=zsmf+zsmfpers(ladyzsmf, ladyzsmfaussagen);}
				if (maidbefragt) {
					zsmf=zsmf+zsmfpers(maidzsmf, maidzsmfaussagen);}
				logger.info("zusammenfassung():"+"ende:"+zsmf);
				return zsmf;
			}
	
	
	//Ende--------------------------------------------------------------------------------------------------------------------------		
		//onSessionEnded() ist das Zeichen für das ende der Session	
			@Override
			public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
				logger.info("OnSessionEnded: start");
				logger.info("OnSessionEnded: Es gab so viele Fehler: "+fehleranzahl);
				logger.info("OnSessionEnded: Es gab folgende Fehler: "+fehler);
				logger.info("OnSessionEnded: ende");
			}
			
	}