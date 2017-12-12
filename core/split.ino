#ifndef Split
#define Split
void split(const String str, char delim, const int limit) {
	char* line = str.c_str();
	int base = 0, i = 0, cpt = 0, anti = 0;
	char[s.size()][s.size()] values;
	for(i = 0; i < str.size(); i++)
	{
		if(line[i] == "\"" && ((anti&1==1)))
			cpt++;
		else if(line[i] == "\\")
			anti++;
		if(cpt > 0 && ((cpt & 1)==0))
		{
			anti = 0;
			cpt = 0;
			strcpy(values[j++], item.substr(base, i).c_str());
			base = i + 1;
		}
	}
	return values;
}

#endif