/**
* Main class for the question of the day discord bot written in scala
* comments formatted with colors using VS Code Colorful Comments extension
**/

//TODO different naming convention to avoid conflicting w/ library/api behavior
//TODO this is bc we can then store additional info we may need in same object 
//TODO thoughts: give Bot a name that's clearly a name and all stuff will be: botnameObject ex: botnameSlashCommand

/*
** Interface for different API commands, if we need different info from them 
TODO convert slashcommand class to this general class if they all always have all the same fields/elements/behaviors in general, and just have 3 separate enums one for each type to hardcode as needed
*/
trait BotCommand:
   def commandId = commandId;
    def commandName = commandName;
    def commandType = commandType;
end BotCommand

/* Dataobject class to store slashcommand info locally from api
** Used for reading and writing data 
TODO: add in other parameterized data for formatting and doing commands before writing them to discord, make design decisions about commands before doing so, test w/ guilds first
*/
class SlashCommand extends BotCommand (
  // internal identifier from API, will be read in & we initialize as NULL
  commandId:Integer, 
  /*
  ** 1-32 character string name users type/see
  */
  commandName:String, 
  /*
  ** 1 - Global Command
  ** 2- Guild/User Command
  ** 3 - Message Command
  */
  commandType:int): 


  def this() = this(null, "", 1)

  def this(commandId:Integer, commandName:String) =
    this(commandId, commandName, 1)

  def this(commandId:Integer, commandName:String, commandType:int) =
    this(commandId, commandName, commandType)


    // TODO consult documentation & ensure way to distinguish slashcommand from guild command, may need two methods here or different returns??
    def getBuilder = SlashCommandBuilder().setName(commandName).setDescription("").setOptions("").set;
end SlashCommand

/*
** Enum that holds all hard-coded initialization for slash commands
** NOTES:
** commandId should initialize as null and will get set from API later
** commandType will default to 1 (Global Command), unless specified
*/
enum SlashCommands {
    case HELPCOMMAND extends SlashCommand(null, "help"),
    case SUBMITQ extends SlashCommand(null, "submitq"),
    case APPROVEQ extends SlashCommand(null, "approveq"),
    case CREATECHANNEL extends SlashCommand(null, "createqotdchannel"),
    case _ None
}

/*
** Enum that holds all hard-coded initialization for message commands
** NOTES:
** looks like slashcommands could almost be reused but I think they're behavior will need to be separate
** 
*/
enum MessageCommands {
  case PINGCOMMAND extends BotCommand(null, "!ping", 3)
  case HELPCOMMAND extends BotCommand(null, "!help", 3)
}

/*
** unsure if needed, thinking no, slashcommands repurposed for these dependent on isTestingMode
*/
enum GuildCommands {

}

val isTestingMode = true; // testing parameter to set in places where we need to distinguish different data bc of discord api behavior/restrictions

// Main method
@main def hello: Unit = {

    //starting up the bot, gathering api data needed and creating any other needed
    // state behavior
    val (api):(DiscordApi) = initializeStartup()

    // Event Listeners:

    // * Message Listeners
    // ^ should generally have equivalent slash command if created, discord is requiring bots
    // ^ used in more than 25 servers to utilize slash commands and NOT messages
    // ^ if this becomes finished obviously coding for long-term and slash should be default
  api.addMessageCreateListener(event => {
    val interaction = event.getMessageCommandInteraction()
      event.getMessageContent match
        case MessageCommands.PINGCOMMAND.getCommandName() => doPing(event)
        case MessageCommands.HELPCOMMAND.getCommandName() => doMsgHelpSummary(event)
        case _ => event.getChannel.sendMessage("Invalid Command")
    })

    // * Slash Command Listeners
    // ! need to have some response within 3 seconds or command will fail. Use respondLater() for 15min response window & send a followup message if needed
  api.addSlashCommandCreateListener(event => {
    val interaction = event.getSlashCommandInteraction()
    
    slashCommands.interaction.getCommandId() match 
      case SlashCommands.HELPCOMMAND.commandName() => doSlashHelpSummary(event)
      case SlashCommands.SUBMITQ.commandName() => submitQOTDQuestion(event)
      case _ => event.getChannel.sendMessage("Invalid command");
  })

  //TODO other main calls/behaviors here

}

// Bot Startup
def initializeStartup(): () = {
  // getting & validating bot token, and logging the bot in
  val token = Source.fromResource("token.txt").getLines().nextOption() match {
    case Some(text) => text
    case None => throw Exception("No token found")
  }
  val api = new DiscordApiBuilder().setToken(token).login.join

  // want to use guild commands for testing purposes, as they update instantly, and global commands
  // only when they're ready for public use since they are cached for 1 hour
  if (isTestingMode) {
    //TODO: make guild commands here for initiating testing
    for i <- SlashCommands do SlashCommands.commandType = 2; //type 2 is a user command, and a 'guild' command per discord api documentation
  } 

    val globalcommands = api.getGlobalSlashCommands();
    for 
          (gcommand <- globalCommands) 
        do {
          if (SlashCommands.gcommand.getCommandName() != None) {
            if (SlashCommands.gcommand.getCommandName().getCommandId() == null)
              SlashCommands.gcommand.getCommandName().commandId = gcommand.getCommandId();
          }
        }

      // if not equal, need to write them to the API
      // TODO - shouldn't just be on not equal? or is else case enough?
      if (SlashCommands.size() != globalCommands.size()) {
      
        List builders = new List();
        for 
          (slashcmd : SlashCommands)
        do {
          if (slashcmd.commandId == null) {
            builders.add(slashcmd.getBuilder());
          }      
        }
        api.bulkOverwriteGlobalApplicationCommands(builders).join();
      } else {
        List builders = new List();
        /*
         TODO
          -if equal still do a sanity check that all slashes now have ID, can't imagine they don't but collect and write them if so
        */
      }

   //TODO do any other start-up state stuff here

  }

    /* Slash Command Behaviors */
  def doHelpSummary(event) : () = {

  }

  defsubmitQOTDQuestion(event) : () = {

  }


    /* Message Command Behaviors */
  def doMsgHelpSummary(event) : () = {

  }

def doPing(event) : () = {

}