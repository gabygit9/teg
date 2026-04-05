import {Country} from '../../enums/Country';
import {Continent} from '../../enums/Continent';
import {CountryGameDTO} from './CountryGame';
/*
export interface MapCountry {
  width: number;
  height: number;
  svgPath?: string;
  color: string;
  rotate: number;
  hovered?: boolean;
  country: Country;
  continente : Continent;
  x: number;
  y: number;
}

 */

export interface MapCountry extends CountryGameDTO {
  svgPath: string;
  x: number;
  y: number;
  width: number;
  height: number;
  rotate: number;
  color: string;
  hovered?: boolean;
  isSelectedOrigin?: boolean;
  isSelectedDestine?: boolean;
  isSelectedArmy?: boolean;
}
