package modules

import play.api.inject._
import services.Scheduler

class Module extends SimpleModule(
  bind[Scheduler].toSelf.eagerly()
)