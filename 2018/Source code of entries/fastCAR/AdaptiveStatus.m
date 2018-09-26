function status = AdaptiveStatus(ReferencePoints, Active_ReferencePoints, Global, direction)
%ISSTABLE Telling if the reference points are properly taken
persistent recent_active_number recent_total_number consecutive_stable_counter threshold old_Active_ReferencePoints;
if strcmp(direction, 'clear')
    consecutive_stable_counter = 0;
end
if isempty(consecutive_stable_counter) %Initialization
    consecutive_stable_counter = 0;
    recent_total_number = 0;
    recent_active_number = 0;
    if Global.M == 3
        threshold = 2;
    elseif Global.M == 5
        threshold = 5;
    elseif Global.M == 10
        threshold = 25;
    elseif Global.M == 15
        threshold = 40;
    else
        threshold = Global.D + Global.M - 12;
    end
    old_Active_ReferencePoints = [];
end
if size(Active_ReferencePoints, 1) >= round(0.95 * Global.N) && size(ReferencePoints, 1) == Global.N
    status = 'ideal';
    return;
end
if size(Active_ReferencePoints, 1) > Global.N && size(ReferencePoints, 1) > Global.N
    status = 'stable';
    return;
end
if size(Active_ReferencePoints, 1) == size(ReferencePoints, 1) && size(ReferencePoints, 1) < Global.N
    status = 'stable';
    return;
end
if size(ReferencePoints, 1) == recent_total_number
    if size(Active_ReferencePoints, 1) ~= recent_active_number %Found more active reference points 
        consecutive_stable_counter = 0;
        recent_active_number = size(Active_ReferencePoints, 1);
        status = 'unstable';
        return;
    elseif size(Active_ReferencePoints, 1) == recent_active_number
        consecutive_stable_counter = consecutive_stable_counter + 1;
        if consecutive_stable_counter >= threshold
            status = 'stable';
            return;
        else
            status = 'unstable';
            return;
        end
    end
else %number of total points changed
    recent_active_number = size(Active_ReferencePoints, 1);
    recent_total_number = size(ReferencePoints, 1);
    consecutive_stable_counter = 0;
    status = 'unstable';
	return;
end