package scheduler

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import javax.inject.{Inject, Singleton}

@Singleton
class RestaurantMailServiceScheduler @Inject()(guestService: GuestService){

  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  scheduler.scheduleAtFixedRate(() => guestService.fetchGuestListAndSendMenu(), 0, 1, TimeUnit.DAYS)

}
