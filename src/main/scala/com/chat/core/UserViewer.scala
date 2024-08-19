package com.chat.core

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserViewer @Inject() (schemas: Set[AbstractSchema[UserCtx]])(implicit ec: ExecutionContext)
    extends ViewerSchema[UserCtx](schemas = schemas.toSeq) {}
