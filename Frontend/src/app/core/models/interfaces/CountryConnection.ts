import {Country} from '../../enums/Country';

export interface CountryConnection {
  id:number;
  originCountry: Country;
  destinationCountry: Country;
}
