import scala.io.Source
import org.javacord.api.*
import org.javacord.api.entity.server.Server
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.interaction.{ApplicationCommand, SlashCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionBuilder, SlashCommandOptionType}

import scala.jdk.javaapi.CollectionConverters
import scala.jdk.OptionConverters.*
import scala.jdk.CollectionConverters.*

object Main {
  @main def entry(): Unit = {
    api.getServers.forEach(println)
    //val slashcommands: Map[Long, Command] = prepareCommands()
    val slashcommands = prepareServerCommands(
      api.getServerById().asScala.getOrThrow(Exception("Not in the testing server")),
      commands
    )

    //message listener attributes
    api.addMessageCreateListener(event => {
      if event.getMessageAuthor.getId != botId then event.getMessageContent match
        case "!ping" => doPing(event)
        case "!help" => doHelpSummary(event)
        case _       => event.getChannel.sendMessage("Invalid Command")
    })

    api.addSlashCommandCreateListener(event => {
      val interaction = event.getSlashCommandInteraction
      slashcommands get interaction.getCommandId foreach { _.handler(interaction) }
    })
  }
}

lazy val token: String =
  Source
    .fromResource("token.txt")
    .getLines()
    .nextOption()
    .getOrThrow(Exception("No Token Found"))

lazy val api: DiscordApi =
  DiscordApiBuilder()
    .setToken(token)
    .login
    .join

lazy val botId: Long = api.getClientId

lazy val commands: Seq[Command] = Seq(
  Command("server", "a command for the server")(),
  Command(
    name ="test",
    description ="a command with arguments",
    options = Seq(
      CommandOption.Str("echo", "to be echoed"),
      CommandOption.Str("private", "won't be echoed")
    )
  ) { interaction =>
    val args = interaction.getArguments
    val echo = args.get(0).getStringValue.toScala
    val priv = args.get(1).getStringValue.toScala
    println(s"private message from test $priv")
    interaction.createImmediateResponder().setContent(s"echoing: $echo").respond.join
  },
  new CommandOOP("test3", "a command defined with inheritance"){
    override def handleInteraction(interaction: SlashCommandInteraction): Unit = {
      interaction.createImmediateResponder().setContent("Hello from Oop!").respond.join
    }
  }
)

def prepareCommands(): Map[Long, Command] = {
  val globalCommands = updateGlobalCommands(commands)
  genCommandIdMap(commands, globalCommands)
}

def prepareServerCommands(server: Server, commands:Seq[Command]): Map[Long, Command] = {
  val serverCommands = updateServerCommands(server, commands)
  genCommandIdMap(commands, serverCommands)
}

def updateGlobalCommands(commands: Seq[Command]): Seq[SlashCommand] = {
  val globalCommands = api.getGlobalSlashCommands.join
  if (globalCommands.size != commands.size) {
    api.bulkOverwriteGlobalApplicationCommands(commands.map { _.slashCommand }).join
    api.getGlobalSlashCommands.join
  } else globalCommands
}

def updateServerCommands(server:Server, commands: Seq[Command]): Seq[SlashCommand] = {
  val serverCommands = api.getServerSlashCommands(server).join
  if (serverCommands.size != commands.size) {
    api.bulkOverwriteServerApplicationCommands(server, commands.map { _.slashCommand }).join
    api.getServerSlashCommands(server).join
  } else serverCommands
}

def genCommandIdMap(localCommands: Seq[Command], globalCommands: Seq[SlashCommand]): Map[Long, Command] = {
  val idMap = globalCommands.map { x => (x.getId, x.getName) }.toMap
  val nameMap = localCommands.map { x => (x.name, x) }.toMap
  idMap.flatMap { (id, name) => // associate the command of every name recognize to its ID
    nameMap.get(name) match {
      case Some(n) => Map(id -> n)
      case None    => Map()
    }
  }
}

// Add a listener which answers with "Pong!" if someone writes "!ping"
def doPing(event : MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("Pong!")
}

def doHelpSummary(event: MessageCreateEvent): Unit = {
  event.getChannel.sendMessage("insert summary of commands here.")
}

class Command(val name:String,
              val description: String = "",
              val options: Seq[CommandOption] = Seq()
             )
             (val handler: SlashCommandInteraction => Unit = _ => {}){
  val slashCommand: SlashCommandBuilder =
    SlashCommandBuilder()
      .setName(name)
      .setDescription(description)
      .setOptions(options.map { _.slashCommandOption })
}

abstract class CommandOOP(val name: String, val description: String = "", val options: Seq[CommandOption] = Seq())
{
  def handleInteraction(interaction: SlashCommandInteraction): Unit
  val slashCommand: SlashCommandBuilder =
    SlashCommandBuilder()
      .setName(name)
      .setDescription(description)
      .setOptions(options.map { _.slashCommandOption })
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

  val slashCommandOption: SlashCommandOption = SlashCommandOption.create(typ, name, description)
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