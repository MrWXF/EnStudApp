import { setupServer } from 'msw/node'
import { authHandlers } from './handlers/auth'
import { wordHandlers } from './handlers/word'
import { chatHandlers } from './handlers/chat'
import { writingHandlers } from './handlers/writing'
import { forumHandlers } from './handlers/forum'
import { readingHandlers } from './handlers/reading'
import { dashboardHandlers } from './handlers/dashboard'
import { translateHandlers } from './handlers/translate'

export const server = setupServer(
  ...authHandlers,
  ...wordHandlers,
  ...chatHandlers,
  ...writingHandlers,
  ...forumHandlers,
  ...readingHandlers,
  ...dashboardHandlers,
  ...translateHandlers,
)
