import StartingStoppingActors.Parent.StartChild
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object StartingStoppingActors extends App{

  val system = ActorSystem("StartingActorsDemo")

  object Parent{
    case class StartChild(name:String)
    case class StopChild(name:String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging{
    import Parent._
    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive={
      case StartChild(name) =>
        log.info(s"Starting child $name")
        context.become(withChildren(children + (name -> context.actorOf(Props[Child],name))))
      case StopChild(name) =>
        log.info(s"Stopping child with name $name")
        val childOption = children.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(message.toString)
    }

  }

  class Child extends Actor with ActorLogging{
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  import Parent._
  val parent = system.actorOf(Props[Parent],"parent")
  parent ! StartChild("child1")
  val child = system.actorSelection("user/parent/child1")
  child ! "hi, kid!"

  parent ! StopChild("child1")

//  for(_ <- 1 to 50) child ! "are you still there ?"

  parent ! StartChild("child2")

  val child2 = system.actorSelection("user/parent/child2")
  child2 ! "hi, second child"
  parent ! Stop
  for(_ <-1 to 10) parent ! "parent,are you still there"
  for(i <-1 to 100) parent ! s"[$i]Second kid, are you still alive?"
}