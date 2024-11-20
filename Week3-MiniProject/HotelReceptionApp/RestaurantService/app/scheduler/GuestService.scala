package scheduler

import models.{GuestDao}
import utils.MailUtil.composeAndSendEmailAllGuests

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class GuestService @Inject()(guestRepository: GuestDao)(implicit ec: ExecutionContext) {

  def fetchGuestListAndSendMenu(): Unit = {
    guestRepository.findActiveGuests().map(composeAndSendEmailAllGuests(_))
  }
}
