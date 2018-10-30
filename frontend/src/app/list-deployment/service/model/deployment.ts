export class Deployment {
  constructor(
    public id: number,
    public name: string,
    public namespace: string,
    public resourceUid: string,
    public resourceVersion: string
  ) {}
}
