import scala.io.Source
import org.javacord.api.*
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.interaction.{ApplicationCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionType}

import scala.jdk.javaapi.CollectionConverters
import scala.jdk.OptionConverters.*
import scala.jdk.CollectionConverters.*

object Main {
  @main def entry: Unit = {

    val (api, slashcommands): (DiscordApi, Map[Long, Option[Command]]) = initializeStartup()

    //message listener attributes
    api.addMessageCreateListener(event => {
      event.getMessageContent match
        case "!ping" => doPing(event)
        case "!help" => doHelpSummary(event)
        case _ => event.getChannel.sendMessage("Invalid Command")
    })

    api.addSlashCommandCreateListener(event => {
      val interaction = event.getSlashCommandInteraction
      slashcommands // Map[Long, Option[Command]]
        .get(interaction.getCommandId) // -> Option[Option[Command]]
        .flatten // -> Option[Command]
        .foreach { _.handler(interaction) } // If it exists, apply it's handler
    })
  }
}

def initializeStartup(): (DiscordApi, Map[Long, Option[Command]]) = {
  val token =
    Source
      .fromResource("token.txt")
      .getLines()
      .nextOption()
      .getOrThrow(Exception("No Token Found"))

  val api =
    DiscordApiBuilder()
      .setToken(token)
      .login
      .join

  val commands = initializeCommands(api)
  val globalCommands = api.getGlobalSlashCommands.join
//  if (globalCommands.size() == 0 || globalCommands.size() != commands.size) {
//      api.bulkOverwriteGlobalApplicationCommands(CollectionConverters.asJava(commands.map(_.slashCommand)))
//  }
//
//  val commandIdMap =
//    api
//      .getGlobalSlashCommands
//      .join
//      .asScala.toSeq
//      .map { x => (x.getId, x.getName) }
//      .toMap

  // this is /by far/ the worse way to do this, but it does save on an api call
  val commandIdMap =
    (if (globalCommands.size() == 0 || globalCommands.size() != commands.size) {
      api.bulkOverwriteGlobalApplicationCommands(commands.map { _.slashCommand })
      api.getGlobalSlashCommands.join
    } else {
      globalCommands
    }).map { x => (x.getId, x.getName) }
      .toMap


  val commandNameMap = commands.map { x => (x.name, x) }.toMap
  val commandMap = commandIdMap.map { (id, name) => (id, commandNameMap.get(name)) }
  (api, commandMap)
}

// Add a listener which answers with "Pong!" if someone writes "!ping"
def doPing(event : MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("Pong!")
}

def doHelpSummary(event: MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("insert summary of commands here.")
}

  /*
  * Bulk writing commands, as advised to ideally only do 1 request
  */
def initializeCommands(api: DiscordApi/*, commands: Seq[Command]*/): List[Command] = {
    val commands: List[Command] = List(
      Command("server", "a command for the server")(),
      Command(
        name ="test",
        description ="a command with arguments",
        options = Seq(
          CommandOption.Str("echo", "to be echoed"),
          CommandOption.Str("private", "won't be echoed")
        )) { interaction =>
          val args = interaction.getArguments
          val echo = args.get(0).getStringValue.toScala
          val priv = args.get(1).getStringValue.toScala
          println(s"private messatge from test $priv")
          interaction.createImmediateResponder().setContent(s"echoing: $echo").respond()
        },
      new CommandOOP("test3", "a command defined with inheritance"){
        override def handleInteraction(interaction: SlashCommandInteraction): Unit = {
          interaction.createImmediateResponder().setContent("Hello from Oop!")
        }
      }
    )
   commands
}

enum CommandOption(val name:String, val description: String, val typ: SlashCommandOptionType)
{
  case Boolean (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.BOOLEAN)
  case Decimal (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.DECIMAL)
  case Long (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.LONG)
  case Mentionable (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.MENTIONABLE)
  case Role (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.ROLE)
  case Str (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.STRING)
  case Sub_Command (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.SUB_COMMAND)
  case Sub_Command_Group (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.SUB_COMMAND_GROUP)
  case Unknown (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.UNKNOWN)
  case User (private val n: String, private val desc: String)
    extends CommandOption(n, desc, SlashCommandOptionType.USER)

  val commandOption: SlashCommandOption = SlashCommandOption.create(typ, name, description)
}


class Command(
               val name:String,
               val description: String = "",
               val options: Seq[CommandOption] = Seq()
             )(val handler: SlashCommandInteraction => Unit = _ => {}){
  val slashCommand: SlashCommandBuilder =
    SlashCommandBuilder()
      .setName(name)
      .setDescription(description)
      .setOptions(options.map { _.commandOption })
}


abstract class CommandOOP(val name: String, val description: String = "", val options: Seq[CommandOption] = Seq())
{
  def handleInteraction(interaction: SlashCommandInteraction): Unit
}

given Conversion[CommandOOP, Command] with
  def apply(cmd: CommandOOP): Command = Command(cmd.name, cmd.description, cmd.options)(cmd.handleInteraction)

given Conversion[Command, CommandOOP] with
  def apply(cmd: Command): CommandOOP = new CommandOOP(cmd.name, cmd.description, cmd.options){
    override def handleInteraction(interaction: SlashCommandInteraction): Unit = cmd.handler(interaction)
  }

given [A]: Conversion[Seq[A], java.util.List[A]] with
  def apply(s: Seq[A]): java.util.List[A] = CollectionConverters.asJava(s)

given [A]: Conversion[java.util.List[A], Seq[A]] with
  def apply(l: java.util.List[A]): Seq[A] = l.asScala.toSeq

extension[A] (opt: Option[A])
  def getOrThrow(e: => Exception): A = opt match {
    case Some(x) => x
    case None => throw e
  }