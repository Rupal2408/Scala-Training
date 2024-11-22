package scheduler

import models.{GuestDao}
import utils.MailUtil.composeAndSendEmailAllGuests

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class GuestService @Inject()(guestRepository: GuestDao)(implicit ec: ExecutionContext) {

  def fetchGuestListAndSendMenu(): Unit = {
    guestRepository.findActiveGuests().map(composeAndSendEmailAllGuests(_))
  }
}
