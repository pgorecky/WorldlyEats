import * as React from 'react';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import Ionicons from 'react-native-vector-icons/Ionicons';

import Favourites from "./Favourites";
import Dishes from "./Dishes";
import ProfileScreen from "./ProfileScreen";
import {Alert, View} from "react-native";
import {NavigationContainer, useNavigation} from "@react-navigation/native";
import {removeAuthHeader} from "../services/axios_config";

const dishes = "Przepisy";
const favourites = "Ulubione";
const profilName = "Profil";

const Tab = createBottomTabNavigator();

const MainView = ({navigation}) => {
    const handleLogout = async () => {
        try {
            Alert.alert(
                'Potwierdzenie',
                'Czy na pewno chcesz się wylogować?',
                [
                    {
                        text: 'Anuluj',
                        style: 'cancel',
                    },
                    {
                        text: 'Wyloguj',
                        onPress: async () => {
                            await removeAuthHeader();
                            navigation.navigate('WelcomePage');
                        },
                    },
                ],
                { cancelable: false }
            );
        } catch (error) {
            console.error('Error during logout:', error);
        }
    };
    const TopBarNav = () => {
        const navigate = useNavigation();

        return (
            <View style={{
                backgroundColor: '#282828',
                paddingTop: 30,
                paddingLeft: 15,
                paddingRight: 15,
                paddingBottom: 5,
                flexDirection: 'row',
                justifyContent: 'space-between'
            }}>
                <Ionicons
                    name="chevron-back-outline"
                    size={35}
                    color="#1DB954"
                    onPress={() => navigate.goBack()}
                />
                <Ionicons
                    name="enter-outline"
                    size={35}
                    color="#1DB954"
                    onPress={handleLogout}
                />
            </View>

        );
    };
    return (
        <NavigationContainer
            independent={true}>
            <View>
                <TopBarNav/>
            </View>

            <Tab.Navigator
                initialRouteName={dishes}
                screenOptions={({route}) => ({

                    tabBarIcon: ({focused, color, size}) => {

                        let iconName;
                        let rn = route.name;

                        if (rn === dishes) {
                            iconName = focused ? 'restaurant' : 'restaurant-outline';

                        } else if (rn === favourites) {
                            iconName = focused ? 'star' : 'star-outline';

                        } else if (rn === profilName) {
                            iconName = focused ? 'person' : 'person-outline';
                        }

                        return <Ionicons name={iconName} size={size} color={color}/>;
                    },

                    "tabBarActiveTintColor": "#1DB954",
                    "tabBarInactiveTintColor": "grey",
                    "tabBarActiveBackgroundColor": "#282828",
                    "tabBarInactiveBackgroundColor": "#282828",
                    "tabBarLabelStyle": {
                        "paddingBottom": 5,
                        "fontSize": 10
                    },
                    "tabBarStyle": [
                        {
                            "display": "flex"
                        },
                        null
                    ]
                })}>
                <Tab.Screen
                    options={{headerShown: false}}
                    name={dishes} component={Dishes}/>
                <Tab.Screen
                    options={{headerShown: false}}
                    name={favourites} component={Favourites}/>
                <Tab.Screen
                    options={{headerShown: false}}
                    name={profilName} component={ProfileScreen}/>

            </Tab.Navigator>
        </NavigationContainer>
    );
}


export default MainView;