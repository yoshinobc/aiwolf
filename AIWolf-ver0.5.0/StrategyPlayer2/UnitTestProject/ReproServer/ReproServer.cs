using AIWolf.Lib;
using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace AIWolf.ReproServer
{
    public class ReproServer
    {

        public int PlayerNo { get; }

        public int CurrentDay { get; set; } = 0;

        public int CurrentTurn { get; set; } = -1;


        GameSetting GameSetting { get; set; }

        GameInfo CurrentGameInfo { get; set; }


        private Dictionary<Agent, Role> hiddenRoleMap;

        private string beforeType = "";
        


        public ReproServer( string logPath, int playerNo )
        {
            PlayerNo = playerNo;

            using ( FileStream fs = new FileStream( logPath, FileMode.Open ) )
            using ( StreamReader sr = new StreamReader( fs ) )
            {
                while ( !sr.EndOfStream )
                {
                    string line = sr.ReadLine();
                    string[] column = line.Split(',');

                    string columnDay = column[0];
                    string columnType = column[1];

                    if ( columnType == "STATUS" )
                    {
                        if ( beforeType != "STATUS" )
                        {
                        }

                    }
                    else
                    {
                        // STATUS行が終了
                    }
                    


                    Console.WriteLine(line);
                }
            }


        }






    }
}
