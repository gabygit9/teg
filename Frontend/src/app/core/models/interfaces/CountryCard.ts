import {Country} from '../../enums/Country';
import {Symbol} from '../../enums/Symbol';

export interface CountryCard {
  id:number;
  country: Country;
  symbol: Symbol;
}
