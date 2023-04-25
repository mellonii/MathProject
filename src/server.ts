// @ts-ignore
import express = require('express');
import type {Db} from "./db/types";
import { dateLog, DialogManager } from './dialog';
import chalk from 'chalk';

export class Dialute {
  dm: DialogManager;
  app: express.Express;
  port: string;

  constructor({ dm, port = '8000' }: { dm: DialogManager; port: string }) {
    this.dm = dm;
    this.app = express();
    this.port = port;

    this.app.use((req, res, next) => {
      dateLog(`${chalk.green(req.method)}: ${chalk.cyan(req.path)}`);
      next();
    });
    this.app.use(express.json());
    this.app.post('/app-connector/', async (request, response) => {
      const body = (await this.dm.process(request.body)).body;
      response.send(body);
    });
  }

  static fromEntrypoint(entrypoint: GeneratorFunction): Dialute {
    const dm = new DialogManager(entrypoint);
    const port = '8000';
    return new Dialute({ dm, port });
  }

  shareApp(path: string) {
    this.app.use(express.static(path));
    return this;
  }

  useDb(db: Db) {
    this.dm.setCtxDb(db);
    return this
  }

  start() {
    this.app.listen(this.port, () =>
      dateLog(chalk.blue(`Start server on http://localhost:${this.port}/`))
    );
  }
}
