import { resumenMock } from "./mockData"

export const fetchResumen = () =>
  new Promise((resolve) => setTimeout(() => resolve(resumenMock), 350))
