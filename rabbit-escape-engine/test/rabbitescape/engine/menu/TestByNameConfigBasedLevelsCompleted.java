package rabbitescape.engine.menu;

import static rabbitescape.engine.util.Util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import rabbitescape.engine.config.Config;
import rabbitescape.engine.config.TestConfig;

import static rabbitescape.engine.menu.ByNameConfigBasedLevelsCompleted.canonicalName;

public class TestByNameConfigBasedLevelsCompleted
{
    @Test
    public void Canonical_name_of_simple_string_is_the_string()
    {
        assertThat( "abcxyz", isEqualToItsCanonicalVersion() );
        assertThat( "c",      isEqualToItsCanonicalVersion() );
        assertThat( "zya",    isEqualToItsCanonicalVersion() );
        assertThat( "1z2a3",  isEqualToItsCanonicalVersion() );
    }

    @Test( expected = ByNameConfigBasedLevelsCompleted.EmptyLevelName.class )
    public void Empty_name_is_not_allowed()
    {
        canonicalName( "" );
    }

    @Test
    public void Spaces_become_underscores()
    {
        assertThat( canonicalName( "abc xyz" ), equalTo( "abc_xyz" ) );
        assertThat( canonicalName( " a " ), equalTo( "_a_" ) );
    }

    @Test
    public void Punctuation_becomes_underscores()
    {
        assertThat( canonicalName( "abc,xyz?" ), equalTo( "abc_xyz_" ) );
    }

    @Test
    public void Upper_case_becomes_lower_case()
    {
        assertThat( canonicalName( "AbcxYZ" ), equalTo( "abcxyz" ) );
    }

    @Test
    public void Unicode_becomes_underscore()
    {
        assertThat(
            canonicalName( "Pile of poo \uD83D\uDCA9 is coo" ),
            equalTo( "pile_of_poo___is_coo" )
        );

        assertThat(
            canonicalName( "Go to \u5317\u4eac\u5e02" ),
            equalTo( "go_to____" )
        );
    }

    @Test
    public void Report_highest_level_from_config_where_some_completed()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        assertThat( lc.highestLevelCompleted( "01_foo" ), equalTo( 3 ) );
    }

    @Test
    public void Report_highest_level_from_config_where_none_completed()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        assertThat( lc.highestLevelCompleted( "02_bar" ), equalTo( 0 ) );
    }

    @Test
    public void Report_highest_level_from_config_where_all_completed()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3",
            "level foo 4",
            "level foo 5",
            "level foo 6",
            "level foo 7",
            "level foo 8",
            "level foo 9",
            "level foo 10"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        assertThat( lc.highestLevelCompleted( "01_foo" ), equalTo( 10 ) );
    }

    @Test
    public void Save_changes_to_config_new_dir()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        lc.setCompletedLevel( "bar", 1 );

        // We called set with the right config key and value
        assertThat(
            fakeConfig.log.get( 1 ),  // 0 was a get
            equalTo(
                "set levels.completed [" +
                "\"level bar 1\"," +
                "\"level foo 1\"," +
                "\"level foo 2\"," +
                "\"level foo 3\"" +
                "]"
            )
        );
        assertThat(
            fakeConfig.log.get( 2 ),
            equalTo( "save" )
        );
        assertThat( fakeConfig.log.size(), equalTo( 3 ) );
    }

    @Test
    public void Save_changes_to_config_existing_dir()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        lc.setCompletedLevel( "foo", 4 );

        assertThat(
            fakeConfig.log.get( 1 ),  // 0 was a get
            equalTo(
                "set levels.completed [" +
                "\"level foo 1\"," +
                "\"level foo 2\"," +
                "\"level foo 3\"," +
                "\"level foo 4\"" +
                "]"
            )
        );
        assertThat(
            fakeConfig.log.get( 2 ),
            equalTo( "save" )
        );
        assertThat( fakeConfig.log.size(), equalTo( 3 ) );
    }

    @Test
    public void No_need_to_update_if_weve_already_completed_a_level()
    {
        FakeConfig fakeConfig = new FakeConfig(
            "level foo 1",
            "level foo 2",
            "level foo 3"
        );

        LevelNames levelNames = new FakeLevelNames(
            levelSet( "foo" ),
            levelSet( "bar" )
        );

        ByNameConfigBasedLevelsCompleted lc =
            new ByNameConfigBasedLevelsCompleted( fakeConfig, levelNames );

        // Two useless calls
        lc.setCompletedLevel( "foo", 3 );
        lc.setCompletedLevel( "foo", 1 );

        // Just gets - nothing saved
        assertThat(
            fakeConfig.log.get( 0 ),
            equalTo( "get levels.completed" )
        );
        assertThat(
            fakeConfig.log.get( 1 ),
            equalTo( "get levels.completed" )
        );
        assertThat( fakeConfig.log.size(), equalTo( 2 ) );
    }

    @Test
    public void Characters_stripped_from_set_names()
    {
        assertThat( ByNameConfigBasedLevelsCompleted.stripNumber_( "01_easy" ),
            equalTo( "easy" ) );
    }

    // ---

    private static class FakeConfig extends Config
    {
        private final String getAnswer;
        public final List<String> log;

        public FakeConfig( String... completedLevelNames )
        {
            super( null, new TestConfig.EmptyConfigStorage() );

            this.getAnswer = makeAnswer( completedLevelNames );
            this.log = new ArrayList<String>();
        }

        private String makeAnswer( String[] completedLevelNames )
        {
            return "["
                + join( ",", map( quoted(), completedLevelNames ) )
                + "]";
        }

        private static Function<String, String> quoted()
        {
            return new Function<String, String>()
            {
                @Override
                public String apply( String t )
                {
                    return "\"" + t + "\"";
                }
            };
        }

        @Override
        public void set( String key, String value )
        {
            log.add( "set " + key + " " + value );
        }

        @Override
        public String get( String key )
        {
            log.add( "get " + key );
            return getAnswer;
        }

        @Override
        public void save()
        {
            log.add( "save" );
        }
    }

    // ---

    private static Matcher<String> isEqualToItsCanonicalVersion()
    {
        return new BaseMatcher<String>()
        {
            String str;

            @Override
            public boolean matches( Object obj )
            {
                if ( !( obj instanceof String ) )
                {
                    return false;
                }

                str = (String)obj;

                return str.equals( canonicalName( str ) );
            }

            @Override
            public void describeTo( Description description )
            {
                description.appendText( canonicalName( str ) );
            }
        };
    }

    private static class LevelSet
    {
        public final String name;
        public final List<String> levelNames;

        public LevelSet( String name, List<String> levelNames )
        {
            this.name = name;
            this.levelNames = levelNames;
        }
    }

    private static LevelSet levelSet( String name )
    {
        return new LevelSet( name, levelNames( name ) );
    }

    private static List<String> levelNames( String name )
    {
        List<String> ret = new ArrayList<String>();

        for ( int i = 1; i < 11; ++i )
        {
            ret.add( "level " + name + " " + i );
        }

        return ret;
    }

    private static class FakeLevelNames implements LevelNames
    {
        private final HashMap<String, List<String>> levelSets;

        public FakeLevelNames( LevelSet... provided )
        {
            levelSets = new HashMap<String, List<String>>();

            for ( LevelSet s : provided )
            {
                levelSets.put( s.name, s.levelNames );
            }
        }

        @Override
        public List<String> namesInDir( String levelsDir )
        {
            return levelSets.get( levelsDir );
        }
    }
}