export class PagingResponse<T> {
  constructor(
    public content: T[],
    public first: boolean,
    public last: boolean,
    public number: number,
    public numberOfElements: number,
    public size: number,
    public totalElements: number,
    public totalPages: number
  ) {}
}
