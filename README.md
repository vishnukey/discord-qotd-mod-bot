A project to create a discord bot designed to help automate a question of the day via allowing anonymous submissions of questions, moderator approval of questions for queueing, and automated posting of the questions at a specific indicated time of day, creating a new thread in the indicated channel for each daily question.


## Project Plan
**Initial Basic Functionality hierarchy to get working in order:**
[[#submit]]
[[#approve]]
[[#Config]]  ```default_setup``` first and extra configs later on, they're lower priority
[[#remove]]
[[#view]] mostly for fun and last priority

### Commands
example input format: ```/qotd config set_verifcation #verification-channel```
- qotd 
	- config
	 	- default_setup
		- set_verification_channel <channel:Channel {required}> 
		- set_question_channel <channel:Channel {required}>
		- set_qotd_role
		- set_qotd_mods_role
		- make_verification_channel <channel:String>
		- make_question_channel <channel:String>
		-  ~~make_qotd_role~~ & ~~make_qotd_mods~~
		- set_verification_requirement
		- set_verifications_needed
		- set_question_output
		- set_time <hour:Integer {required}> <minute:Integer>
	- submit <question:String {required}>
	- approve <uuid:Integer {required} | choices> <isApproved:Boolean {required}>
	- remove_from_queue <uuid:Integer {required} |  choices>
	- view_questions <wantQuestionCount:Boolean> <wantQuestionList:Boolean>
	- view_stats
	- view_when_next_qotd

#### QOTD
##### Config
- **default_setup** <modRole: Role {required}> <wantThreads:Boolean {required}> <botcmds:Channel>
	provides administrators with an automated, and painless bot setup. This will create:
		- A QOTD channel where questions will be populated, gives access to all channel members by default, no roles required
		- A verification channel for moderators to approve questions (visible to only modRole)
		- sets a default time for question posting 12am EST (can be changed with ```set_time```)
		- will configure questions to either spawn threads or just be posted in channel dependent on ```wantThreads```
		- will create a bot command channel for user submissions, unless an existing bot command channel is specified
		- 
- **set_verification_channel** <channel: Channel {required}>
	allows administrators to specify a channel where submitted questions will populate for approval 
- **set_question_channel** <channel:Channel {required}>
	allows administrators to specify their own channel where approved QOTD questions will populate each day
- **set_qotd_role** <qotdRole:Role {required}>
	allow administrators to set an existing role as a qotd role to access ```qotdchannel```
- **set_qotd_mods_role** <qotdMods:Role {required}>
		allow administators to appoint others to access channel for verifying questions
-  **make_verification_channel** <channel:String > <isRestricted:Boolean>
	bot will make a verification channel for administrators with given name, or use a defaulted channel name, and 
- **make_question_channel** <channel:String > <isRestricted:Boolean>
bot will make a question channel for administrators with given name, or use a defaulted channel name, and 

~~- **make_qotd_role** & **make_qotd_mods**~~
- **set_verification_requirement** <wantVerify:Boolean {required}>
	allow administrators to opt out of verification and allow for the chaos of unapproved questions (bot is not responsible for questions not adhereing to discord terms & conditions)
- **set_verifications_needed** <verifyNum:Integer {required}>
number of approvals needed from mods for a question to be entered in the queue 
- **set_question_output** <>
allows administrators to indicate if they want a linear, first in first out, question order, or a randomized distribution of questions weighted by the amount submitted per user to encourage fair distribution of everyone's questions
- **set_time** <hour:Integer {required}> <minute:Integer>
allows administrators to indicate what time of day, and in what time zone they would like QOTDs posted in

##### submit 
- **submit** <question:String {required}>
	submits a question for the question of the day into the queue. when run message users entered will **not** be echoed back to the channel, allowing for anonmity of submissions

##### approve
- **approve** <uuid:Integer {required} | choices> <isApproved:Boolean {required}>
	a manual approval option requiring the internal message ID assigned by the bot, and either ```true``` to indicate approval or ```false``` to indicate rejection
	bot will require x amount of approvals before a question will enter the queue
		when a question is fully approved bot will assign it it's own reaction
		messages will remain in verification channel until their moment in the queue to be posted, then message is deleted in verification & bot posts in qotdchannel
		
##### remove
- **remove_from_queue** <uuid:Integer {required} |  choices>
	allows manual removal of a question from the bot queue in the case that a question is accidentally approved or needs to be removed for any other reason, will be permanently deleted from bot queue and verification channel	

	
##### view
- **view_questions** <wantQuestionCount:Boolean> ~~<wantQuestionList:Boolean>~~
	allows administrators to view the number of questions currently in the bot queue if ```wantQuestionCount = true```  or ~~a list of the current questions if ```wantQuestionList = true```~~
- **view_stats**
	allow users to run to see fun statistics:
		- number of questions they submitted to the bot
		- (if threads, # questions they answered?)
- **view_when_next_qotd**
	- returns how long until the next qotd is posted as of when this command was run in hh:mm:ss


### Bot Passive Functionality
#####QOTD
- if 3+ questions from the same user have been rejected, bot will send a warning out in the verification channel with the user-id & username so that mods are aware and can catch spamming / trolls
- if the question of the day question queue is running low, bot will send a message in the qotd-channel asking users to submit questions
- will send (1) "uh oh, we're empty" message the day after the queue empties, and then not again, to avoid spamming the qotd channel

### Bot privacy:
#### QOTD
- bot tracks submissions by userid in the background, but only for distribution and possible spam detection
	- usernames associated with questions will only be visible to moderators after 3+ questions have been rejected by the moderators